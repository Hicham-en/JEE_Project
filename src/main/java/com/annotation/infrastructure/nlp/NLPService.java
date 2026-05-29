package com.annotation.infrastructure.nlp;

import com.annotation.application.dto.MetricsResultDTO;
import com.annotation.application.dto.NLPRunDTO;
import com.annotation.application.dto.NLPRunLogDTO;
import com.annotation.application.dto.NLPRunRequestDTO;
import com.annotation.application.exception.EntityNotFoundException;
import com.annotation.application.service.INLPService;
import com.annotation.domain.entity.Dataset;
import com.annotation.domain.entity.NLPRun;
import com.annotation.domain.entity.NLPRunStatus;
import com.annotation.domain.entity.NLPRunType;
import com.annotation.domain.repository.DatasetRepository;
import com.annotation.domain.repository.NLPRunRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application-facing NLP service that creates script runs and delegates execution to an async worker.
 */
@Service
public class NLPService implements INLPService {

    private static final Logger log = LoggerFactory.getLogger(NLPService.class);

    private final DatasetRepository datasetRepository;
    private final NLPRunRepository nlpRunRepository;
    private final NlpRunWorker nlpRunWorker;
    private final NlpMetricsParser nlpMetricsParser;
    private final ObjectMapper objectMapper;

    public NLPService(DatasetRepository datasetRepository,
                      NLPRunRepository nlpRunRepository,
                      NlpRunWorker nlpRunWorker,
                      NlpMetricsParser nlpMetricsParser,
                      ObjectMapper objectMapper) {
        this.datasetRepository = datasetRepository;
        this.nlpRunRepository = nlpRunRepository;
        this.nlpRunWorker = nlpRunWorker;
        this.nlpMetricsParser = nlpMetricsParser;
        this.objectMapper = objectMapper;
    }

    /**
     * Starts a training run asynchronously.
     *
     * @param request training parameters
     * @return persisted run snapshot
     */
    @Override
    @Transactional
    public NLPRunDTO runTraining(NLPRunRequestDTO request) {
        NLPRun run = createRun(request, NLPRunType.TRAINING, request.scriptPath());
        nlpRunWorker.executeRun(run.getId(), null);
        log.info("NLP training run queued runId={} datasetId={}", run.getId(), request.datasetId());
        return toDto(run);
    }

    /**
     * Starts a test run asynchronously.
     *
     * @param request test parameters
     * @param trainingRunId optional training run ID
     * @return persisted run snapshot
     */
    @Override
    @Transactional
    public NLPRunDTO runTest(NLPRunRequestDTO request, Long trainingRunId) {
        String scriptPath = request.scriptPath() == null || request.scriptPath().isBlank() ? "test.py" : request.scriptPath();
        NLPRun run = createRun(request, NLPRunType.TEST, scriptPath);
        nlpRunWorker.executeRun(run.getId(), trainingRunId);
        log.info("NLP test run queued runId={} datasetId={} trainingRunId={}", run.getId(), request.datasetId(), trainingRunId);
        return toDto(run);
    }

    /**
     * Returns raw logs for a run.
     *
     * @param runId run ID
     * @return logs content
     */
    @Override
    @Transactional(readOnly = true)
    public String getRunLogs(Long runId) {
        return findRun(runId).getLogs();
    }

    /**
     * Returns the JSON payload expected by polling JavaScript.
     *
     * @param runId run ID
     * @return logs, status and metrics
     */
    @Override
    @Transactional(readOnly = true)
    public NLPRunLogDTO getRunLogPayload(Long runId) {
        NLPRun run = findRun(runId);
        return new NLPRunLogDTO(run.getLogs(), run.getStatus().name(), parseMetricsFromLogs(run.getLogs()));
    }

    /**
     * Returns one NLP run.
     *
     * @param runId run ID
     * @return run details
     */
    @Override
    @Transactional(readOnly = true)
    public NLPRunDTO getRun(Long runId) {
        return toDto(findRun(runId));
    }

    /**
     * Returns all runs for a dataset.
     *
     * @param datasetId dataset ID
     * @return run history
     */
    @Override
    @Transactional(readOnly = true)
    public List<NLPRunDTO> getRunHistory(Long datasetId) {
        return nlpRunRepository.findByDatasetIdOrderByStartTimeDesc(datasetId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Parses metrics from common Python output formats such as "accuracy: 0.92" and "f1=0.88".
     *
     * @param logs Python process output
     * @return parsed metrics
     */
    @Override
    public MetricsResultDTO parseMetricsFromLogs(String logs) {
        if (logs == null || logs.isBlank()) {
            return new MetricsResultDTO(null, null, null);
        }
        return nlpMetricsParser.parse(logs);
    }

    /**
     * Marks an active run as cancelled.
     *
     * @param runId run ID
     */
    @Override
    @Transactional
    public void cancelRun(Long runId) {
        NLPRun run = findRun(runId);
        if (run.getStatus() == NLPRunStatus.RUNNING || run.getStatus() == NLPRunStatus.PENDING) {
            run.setStatus(NLPRunStatus.CANCELLED);
            run.setLogs((run.getLogs() == null ? "" : run.getLogs()) + "\nCancellation requested by administrator.\n");
            nlpRunRepository.save(run);
        }
    }

    private NLPRun createRun(NLPRunRequestDTO request, NLPRunType type, String scriptPath) {
        Dataset dataset = datasetRepository.findById(request.datasetId())
                .orElseThrow(() -> new EntityNotFoundException("Dataset not found"));
        NLPRun run = NLPRun.builder()
                .dataset(dataset)
                .type(type)
                .status(NLPRunStatus.PENDING)
                .scriptPath(scriptPath)
                .params(toJson(paramsFor(request)))
                .logs("")
                .build();
        return nlpRunRepository.save(run);
    }

    private Map<String, Object> paramsFor(NLPRunRequestDTO request) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("learningRate", request.learningRate());
        params.put("epochs", request.epochs());
        params.put("batchSize", request.batchSize());
        return params;
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private NLPRun findRun(Long runId) {
        return nlpRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("NLP run not found"));
    }

    private NLPRunDTO toDto(NLPRun run) {
        return new NLPRunDTO(
                run.getId(),
                run.getDataset().getId(),
                run.getType().name(),
                run.getStatus().name(),
                run.getStartTime(),
                run.getEndTime(),
                run.getDurationSeconds(),
                run.getScriptPath(),
                run.getParams(),
                run.getLogs(),
                run.getAccuracy(),
                run.getF1Score(),
                run.getConfusionMatrix()
        );
    }
}
