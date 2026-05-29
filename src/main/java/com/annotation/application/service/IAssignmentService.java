package com.annotation.application.service;

import com.annotation.application.dto.AssignmentResultDTO;
import java.util.List;

/**
 * Service port for annotator task assignment.
 */
public interface IAssignmentService {

    /**
     * Assigns multiple annotators to a dataset.
     *
     * @param datasetId   dataset to assign
     * @param annotatorIds list of annotator IDs
     * @return assignment result
     */
    AssignmentResultDTO assignAnnotators(Long datasetId, List<Long> annotatorIds);

    /**
     * Removes an annotator from a dataset.
     *
     * @param datasetId   dataset ID
     * @param annotatorId annotator ID
     * @return success boolean
     */
    boolean unassignAnnotator(Long datasetId, Long annotatorId);
}
