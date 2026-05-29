package com.annotation.application.service;

import com.annotation.application.dto.MetricsDTO;
import com.annotation.application.dto.SpammerDTO;
import java.util.List;

/**
 * Service port for calculating quality metrics and detecting invalid behavior.
 */
public interface IMetricsService {

    /**
     * Calculates Cohen's Kappa score for two annotators on a dataset.
     *
     * @param datasetId dataset ID
     * @param annotatorId1 first annotator
     * @param annotatorId2 second annotator
     * @return agreement score between -1.0 and +1.0
     */
    double computeCohenKappa(Long datasetId, Long annotatorId1, Long annotatorId2);

    /**
     * Calculates Fleiss' Kappa score for all annotators on a dataset.
     *
     * @param datasetId dataset ID
     * @return metric DTO containing agreement score and interpretation
     */
    MetricsDTO computeFleissKappa(Long datasetId);

    /**
     * Detects potentially spamming annotators on a given dataset.
     *
     * @param datasetId dataset ID
     * @return list of flagged annotators
     */
    List<SpammerDTO> detectSpammers(Long datasetId);
}
