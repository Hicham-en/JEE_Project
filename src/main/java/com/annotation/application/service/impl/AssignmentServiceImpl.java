package com.annotation.application.service.impl;

import com.annotation.application.dto.AssignmentResultDTO;
import com.annotation.application.exception.AssignmentException;
import com.annotation.application.exception.EntityNotFoundException;
import com.annotation.application.service.IAssignmentService;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.Dataset;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.TaskStatus;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.AnnotatorRepository;
import com.annotation.domain.repository.DatasetRepository;
import com.annotation.domain.repository.TaskRepository;
import com.annotation.domain.repository.TextPairRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl implements IAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AssignmentServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TextPairRepository textPairRepository;
    private final AnnotatorRepository annotatorRepository;
    private final DatasetRepository datasetRepository;

    public AssignmentServiceImpl(TaskRepository taskRepository,
                                 TextPairRepository textPairRepository,
                                 AnnotatorRepository annotatorRepository,
                                 DatasetRepository datasetRepository) {
        this.taskRepository = taskRepository;
        this.textPairRepository = textPairRepository;
        this.annotatorRepository = annotatorRepository;
        this.datasetRepository = datasetRepository;
    }

    @Override
    @Transactional
    public AssignmentResultDTO assignAnnotators(Long datasetId, List<Long> annotatorIds) {
        if (annotatorIds == null || annotatorIds.isEmpty()) {
            throw new AssignmentException("Sélectionnez au moins un nouvel annotateur");
        }

        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset introuvable"));

        long activeAssignedAnnotators = taskRepository.findByDatasetId(datasetId).stream()
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .map(task -> task.getAnnotator().getId())
                .distinct()
                .count();

        if (activeAssignedAnnotators + annotatorIds.size() < 3) {
            throw new AssignmentException("Au moins 3 annotateurs doivent être assignés au total");
        }

        List<Annotator> annotators = annotatorRepository.findAllById(annotatorIds);
        if (annotators.size() != annotatorIds.size()) {
            throw new AssignmentException("Un ou plusieurs annotateurs sont introuvables");
        }

        Map<Long, Annotator> annotatorsById = annotators.stream()
            .collect(Collectors.toMap(Annotator::getId, annotator -> annotator));
        List<Annotator> orderedAnnotators = annotatorIds.stream()
            .map(annotatorsById::get)
            .collect(Collectors.toList());

        List<TextPair> textPairs = textPairRepository.findByDatasetIdOrderByIdAsc(datasetId);
        if (textPairs.isEmpty()) {
            throw new AssignmentException("Le dataset ne contient aucun texte à assigner");
        }

        // Algo de distribution avec seed fixe (ou basé sur id dataset par ex) pour être reproductible au besoin
        List<TextPair> shuffledPairs = new ArrayList<>(textPairs);
        Collections.shuffle(shuffledPairs, new Random(datasetId)); 

        int n = orderedAnnotators.size();
        
        // Initialiser ou récupérer les Tasks existantes
        List<Task> tasks = new ArrayList<>();
        AtomicInteger nbTachesCreees = new AtomicInteger(0);
        
        for (Annotator annotator : orderedAnnotators) {
            Task task = taskRepository.findByDatasetIdAndAnnotatorId(datasetId, annotator.getId())
                    .map(existing -> {
                        existing.getTextPairs().clear();
                        existing.getAnnotations().clear();
                        existing.setStatus(TaskStatus.PENDING);
                        return existing;
                    })
                    .orElseGet(() -> {
                        nbTachesCreees.incrementAndGet();
                        return Task.builder()
                                .dataset(dataset)
                                .annotator(annotator)
                                .status(TaskStatus.PENDING)
                                .textPairs(new ArrayList<>())
                                .build();
                    });
            tasks.add(task);
        }

        // Distribution Round-robin : pour chaque TextPair, on distribue à 3 annotateurs consécutifs
        for (int i = 0; i < shuffledPairs.size(); i++) {
            TextPair tp = shuffledPairs.get(i);
            for (int k = 0; k < 3; k++) {
                int annotatorIndex = (i + k) % n;
                Task t = tasks.get(annotatorIndex);
                if (!t.getTextPairs().contains(tp)) {
                    t.getTextPairs().add(tp);
                }
            }
        }

        taskRepository.saveAll(tasks);
        log.info("Annotators assigned datasetId={} annotatorCount={} createdTasks={}", datasetId, n, nbTachesCreees.get());

        // Approximate number of texts per annotator
        int nbTextsPerAnnotator = (textPairs.size() * 3) / n;

        return new AssignmentResultDTO(n, nbTachesCreees.get(), nbTextsPerAnnotator);
    }

    @Override
    @Transactional
    public boolean unassignAnnotator(Long datasetId, Long annotatorId) {
        return taskRepository.findByDatasetIdAndAnnotatorId(datasetId, annotatorId)
                .map(task -> {
                    task.getAnnotations().clear();
                    task.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(task);
                    log.info("Annotator unassigned datasetId={} annotatorId={}", datasetId, annotatorId);
                    return true;
                })
                .orElse(false);
    }
}
