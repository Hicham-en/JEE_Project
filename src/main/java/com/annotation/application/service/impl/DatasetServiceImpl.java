package com.annotation.application.service.impl;

import com.annotation.application.dto.DatasetCreateDTO;
import com.annotation.application.dto.DatasetDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.dto.TextPairDTO;
import com.annotation.application.exception.DatasetImportException;
import com.annotation.application.exception.EntityNotFoundException;
import com.annotation.application.mapper.DatasetMapper;
import com.annotation.application.service.IDatasetService;
import com.annotation.domain.entity.Dataset;
import com.annotation.domain.entity.PossibleClass;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.DatasetRepository;
import com.annotation.domain.repository.TextPairRepository;
import com.annotation.infrastructure.parser.CsvParser;
import com.annotation.infrastructure.parser.JsonParser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Dataset service.
 */
@Service
public class DatasetServiceImpl implements IDatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetServiceImpl.class);

    private final DatasetRepository datasetRepository;
    private final TextPairRepository textPairRepository;
    private final DatasetMapper datasetMapper;
    private final CsvParser csvParser;
    private final JsonParser jsonParser;

    @PersistenceContext
    private EntityManager entityManager;

    public DatasetServiceImpl(DatasetRepository datasetRepository,
                              TextPairRepository textPairRepository,
                              DatasetMapper datasetMapper,
                              CsvParser csvParser,
                              JsonParser jsonParser) {
        this.datasetRepository = datasetRepository;
        this.textPairRepository = textPairRepository;
        this.datasetMapper = datasetMapper;
        this.csvParser = csvParser;
        this.jsonParser = jsonParser;
    }

    @Override
    @Transactional
    public DatasetDTO createDataset(DatasetCreateDTO dto, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DatasetImportException("File cannot be empty");
        }

        // Parse file
        List<TextPairDTO> textPairDTOs;
        try {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".json")) {
                textPairDTOs = jsonParser.parse(file.getInputStream());
            } else {
                textPairDTOs = csvParser.parse(file.getInputStream());
            }
        } catch (Exception e) {
            throw new DatasetImportException("Failed to parse file: " + e.getMessage(), e);
        }

        // Create Dataset
        Dataset dataset = new Dataset(dto.nom(), dto.description(), dto.langue());

        // Handle PossibleClasses
        if (dto.classes() == null || dto.classes().isBlank()) {
            throw new DatasetImportException("At least one possible class must be provided");
        }

        List<PossibleClass> classes = Arrays.stream(dto.classes().split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(c -> {
                    PossibleClass pc = new PossibleClass();
                    pc.setLibelle(c);
                    pc.setDataset(dataset);
                    return pc;
                }).collect(Collectors.toList());

            if (classes.isEmpty()) {
                throw new DatasetImportException("At least one possible class must be provided");
            }

        dataset.setPossibleClasses(classes);

        // Save dataset first to get ID
        Dataset savedDataset = datasetRepository.save(dataset);

        // Batch insert TextPairs
        int batchSize = 500;
        for (int i = 0; i < textPairDTOs.size(); i++) {
            TextPairDTO pairDto = textPairDTOs.get(i);
            if (pairDto.text1() == null || pairDto.text1().trim().isEmpty()) {
                continue;
            }

            TextPair textPair = TextPair.builder()
                    .text1(pairDto.text1())
                    .text2(pairDto.text2())
                    .metadata(pairDto.metadata())
                    .dataset(savedDataset)
                    .build();

            entityManager.persist(textPair);

            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
                // Re-merge dataset to keep it managed after clear if needed,
                // but since we only use it as proxy for textPair, getting reference is better:
                savedDataset = entityManager.getReference(Dataset.class, savedDataset.getId());
            }
        }

        entityManager.flush();
        entityManager.clear();

        DatasetDTO resultDto = datasetMapper.toDto(datasetRepository.findById(savedDataset.getId())
            .orElseThrow(() -> new EntityNotFoundException("Dataset not found after save")));
        log.info("Dataset created id={} name={} importedTextPairs={}", savedDataset.getId(), savedDataset.getNom(), textPairDTOs.size());
        return new DatasetDTO(
                resultDto.id(),
                resultDto.nom(),
                resultDto.description(),
                resultDto.langue(),
                resultDto.createdAt(),
                calculateProgress(resultDto.id())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<DatasetDTO> getAllDatasets(Pageable pageable) {
        Page<Dataset> datasetsPage = datasetRepository.findAll(pageable);
        
        List<DatasetDTO> dtos = datasetsPage.getContent().stream().map(d -> {
            DatasetDTO baseDto = datasetMapper.toDto(d);
            return new DatasetDTO(
                    baseDto.id(),
                    baseDto.nom(),
                    baseDto.description(),
                    baseDto.langue(),
                    baseDto.createdAt(),
                    calculateProgress(baseDto.id())
            );
        }).collect(Collectors.toList());

        return new PageResponseDTO<>(
                dtos,
                datasetsPage.getNumber(),
                datasetsPage.getSize(),
                datasetsPage.getTotalElements(),
                datasetsPage.getTotalPages(),
                datasetsPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DatasetDTO getDatasetById(Long id) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));
        
        DatasetDTO baseDto = datasetMapper.toDto(dataset);
        return new DatasetDTO(
                baseDto.id(),
                baseDto.nom(),
                baseDto.description(),
                baseDto.langue(),
                baseDto.createdAt(),
                calculateProgress(baseDto.id())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateProgress(Long datasetId) {
        // Use custom JPQL queries in real app. For step 5.1 we calculate (annotations complètes / total textes * 3) * 100
        Long totalTextPairs = textPairRepository.countByDatasetId(datasetId);
        if (totalTextPairs == null || totalTextPairs == 0) {
            return 0.0;
        }

        // Here we ideally count total annotations for this dataset
        // Since we don't have AnnotationRepository injected directly (maybe we should),
        // we will fetch via a unified query or inject it.
        // As a shortcut, query text pairs directly or let's use EntityManager to count.
        
        Long totalAnnotations = entityManager.createQuery(
                "SELECT COUNT(a) FROM Annotation a WHERE a.textPair.dataset.id = :datasetId", Long.class)
                .setParameter("datasetId", datasetId)
                .getSingleResult();
        
        double requiredAnnotations = totalTextPairs * 3.0;
        double progress = (totalAnnotations / requiredAnnotations) * 100.0;
        return Math.min(progress, 100.0);
    }
}
