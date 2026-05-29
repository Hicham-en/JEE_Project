package com.annotation.application.service;

import com.annotation.application.dto.DatasetCreateDTO;
import com.annotation.application.dto.DatasetDTO;
import com.annotation.application.dto.PageResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service port for Dataset operations.
 */
public interface IDatasetService {

    /**
     * Creates a dataset from form inputs and a file.
     *
     * @param dto the creation details
     * @param file the dataset file
     * @return created dataset
     */
    DatasetDTO createDataset(DatasetCreateDTO dto, MultipartFile file);

    /**
     * Gets all datasets with pagination.
     *
     * @param pageable pagination info
     * @return page of datasets
     */
    PageResponseDTO<DatasetDTO> getAllDatasets(Pageable pageable);

    /**
     * Gets dataset by ID.
     *
     * @param id dataset ID
     * @return the dataset
     */
    DatasetDTO getDatasetById(Long id);

    /**
     * Calculates the progress of a dataset.
     *
     * @param datasetId dataset ID
     * @return progress percentage 0.0 - 100.0
     */
    double calculateProgress(Long datasetId);
}
