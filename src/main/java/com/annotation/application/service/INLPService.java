package com.annotation.application.service;

import com.annotation.application.dto.NLPRunDTO;
import com.annotation.application.dto.NLPRunLogDTO;
import com.annotation.application.dto.NLPRunRequestDTO;
import com.annotation.application.dto.MetricsResultDTO;
import java.util.List;

/**
 * Service port for executing NLP scripts and retrieving results.
 */
public interface INLPService {

    /**
     * Starts a training run and returns immediately while the script runs asynchronously.
     *
     * @param request training parameters
     * @return persisted run snapshot
     */
    NLPRunDTO runTraining(NLPRunRequestDTO request);

    /**
     * Starts an evaluation run and returns immediately while the script runs asynchronously.
     *
     * @param request test parameters
     * @param trainingRunId optional training run identifier
     * @return persisted run snapshot
     */
    NLPRunDTO runTest(NLPRunRequestDTO request, Long trainingRunId);

    /**
     * Retrieves logs for a specific run execution.
     *
     * @param runId run ID
     * @return logs content
     */
    String getRunLogs(Long runId);

    /**
     * Retrieves the current polling payload for a run.
     *
     * @param runId run ID
     * @return logs, status and parsed metrics
     */
    NLPRunLogDTO getRunLogPayload(Long runId);

    /**
     * Retrieves a single run by ID.
     *
     * @param runId run ID
     * @return run details
     */
    NLPRunDTO getRun(Long runId);

    /**
     * Gets NLP execution history for a dataset.
     *
     * @param datasetId dataset ID
     * @return run history
     */
    List<NLPRunDTO> getRunHistory(Long datasetId);

    /**
     * Parses script metrics from raw logs.
     *
     * @param logs Python process output
     * @return parsed metrics
     */
    MetricsResultDTO parseMetricsFromLogs(String logs);

    /**
     * Cancels a run if it is still active.
     *
     * @param runId run ID
     */
    void cancelRun(Long runId);
}
