package com.annotation.infrastructure.nlp;

import com.annotation.application.dto.MetricsResultDTO;
import com.annotation.domain.entity.NLPRun;
import com.annotation.domain.entity.NLPRunStatus;
import com.annotation.domain.entity.NLPRunType;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.NLPRunRepository;
import com.annotation.domain.repository.TextPairRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Async worker responsible for running Python scripts and persisting their output.
 */
@Component
public class NlpRunWorker {

    private static final long TIMEOUT_MINUTES = 30L;
    private static final int MAX_LOG_LENGTH = 60000;

    private final NLPRunRepository nlpRunRepository;
    private final TextPairRepository textPairRepository;
    private final NlpMetricsParser nlpMetricsParser;
    private final String pythonExecutable;
    private final Path scriptsPath;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NlpRunWorker.class);

    public NlpRunWorker(NLPRunRepository nlpRunRepository,
                        TextPairRepository textPairRepository,
                        NlpMetricsParser nlpMetricsParser,
                        @Value("${nlp.python-executable:python}") String pythonExecutable,
                        @Value("${nlp.scripts-dir:scripts}") String scriptsDir) {
        this.nlpRunRepository = nlpRunRepository;
        this.textPairRepository = textPairRepository;
        this.nlpMetricsParser = nlpMetricsParser;
        this.pythonExecutable = pythonExecutable;
        String dir = (scriptsDir == null || scriptsDir.isBlank() || scriptsDir.equals("/")) ? "scripts" : scriptsDir;
        Path dirPath = Path.of(dir);
        if (dirPath.isAbsolute()) {
            this.scriptsPath = dirPath.normalize();
        } else {
            this.scriptsPath = Path.of(System.getProperty("user.dir")).resolve(dir).normalize();
        }
        log.info("NLP scripts directory resolved to: {}", this.scriptsPath);
    }

    /**
     * Executes a Python process on the dedicated NLP executor and stores stdout before waiting on process completion.
     *
     * @param runId run to execute
     * @param trainingRunId optional training run ID used by test scripts
     * @return completion future
     */
    @Async("nlpExecutor")
    public CompletableFuture<Void> executeRun(Long runId, Long trainingRunId) {
        NLPRun run = nlpRunRepository.findById(runId).orElseThrow();
        run.setStatus(NLPRunStatus.RUNNING);
        run.setStartTime(LocalDateTime.now());
        run.setLogs("Starting NLP " + run.getType().name().toLowerCase() + " run...\n");
        nlpRunRepository.save(run);

        Process process = null;
        try {
            Path datasetFile = writeDatasetFile(run);
            List<String> command = buildCommand(run, datasetFile, trainingRunId);
            appendLog(runId, "Command: " + String.join(" ", command) + "\n");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            Process runningProcess = process;

            Thread stdoutReader = new Thread(() -> readOutput(runId, runningProcess), "nlp-stdout-" + runId);
            stdoutReader.start();

            boolean finished = waitForProcess(runId, runningProcess);
            if (!finished) {
                runningProcess.destroyForcibly();
                appendLog(runId, "Run timed out after " + TIMEOUT_MINUTES + " minutes.\n");
                finish(runId, NLPRunStatus.FAILED);
            } else {
                stdoutReader.join();
                int exitCode = runningProcess.exitValue();
                NLPRun current = nlpRunRepository.findById(runId).orElseThrow();
                if (current.getStatus() == NLPRunStatus.CANCELLED) {
                    appendLog(runId, "Run cancelled.\n");
                    finish(runId, NLPRunStatus.CANCELLED);
                } else {
                    finish(runId, exitCode == 0 ? NLPRunStatus.COMPLETED : NLPRunStatus.FAILED);
                }
            }
            Files.deleteIfExists(datasetFile);
        } catch (Exception e) {
            if (process != null) {
                process.destroyForcibly();
            }
            appendLog(runId, "Run failed: " + e.getMessage() + "\n");
            finish(runId, NLPRunStatus.FAILED);
        }
        return CompletableFuture.completedFuture(null);
    }

    private List<String> buildCommand(NLPRun run, Path datasetFile, Long trainingRunId) {
        List<String> command = new ArrayList<>();
        command.add(pythonExecutable);
        String scriptPathStr = run.getScriptPath();
        if (scriptPathStr != null && scriptPathStr.startsWith("/")) {
            scriptPathStr = scriptPathStr.substring(1);
        }
        Path scriptFile = scriptsPath.resolve(scriptPathStr).normalize();
        command.add(scriptFile.toString());
        command.add("--dataset");
        command.add(datasetFile.toAbsolutePath().toString());
        if (run.getType() == NLPRunType.TRAINING) {
            command.add("--lr");
            command.add(extractParam(run.getParams(), "learningRate", "0.001"));
            command.add("--epochs");
            command.add(extractParam(run.getParams(), "epochs", "10"));
            command.add("--batch-size");
            command.add(extractParam(run.getParams(), "batchSize", "32"));
        } else if (trainingRunId != null) {
            command.add("--run");
            command.add(String.valueOf(trainingRunId));
        }
        return command;
    }

    private boolean waitForProcess(Long runId, Process process) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(TIMEOUT_MINUTES);
        while (System.nanoTime() < deadline) {
            if (process.waitFor(1, TimeUnit.SECONDS)) {
                return true;
            }
            NLPRun current = nlpRunRepository.findById(runId).orElseThrow();
            if (current.getStatus() == NLPRunStatus.CANCELLED) {
                process.destroyForcibly();
                return true;
            }
        }
        return false;
    }

    private void readOutput(Long runId, Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                appendLog(runId, line + "\n");
            }
        } catch (IOException e) {
            appendLog(runId, "Unable to read process output: " + e.getMessage() + "\n");
        }
    }

    private Path writeDatasetFile(NLPRun run) throws IOException {
        Path tempFile = Files.createTempFile("annotate-nlp-dataset-" + run.getDataset().getId(), ".csv");
        List<TextPair> pairs = textPairRepository.findByDatasetIdOrderByIdAsc(run.getDataset().getId());
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write("id,text1,text2\n");
            for (TextPair pair : pairs) {
                writer.write(pair.getId() + "," + csv(pair.getText1()) + "," + csv(pair.getText2()) + "\n");
            }
        }
        return tempFile;
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void appendLog(Long runId, String line) {
        NLPRun run = nlpRunRepository.findById(runId).orElseThrow();
        String updatedLogs = (run.getLogs() == null ? "" : run.getLogs()) + line;
        if (updatedLogs.length() > MAX_LOG_LENGTH) {
            updatedLogs = updatedLogs.substring(updatedLogs.length() - MAX_LOG_LENGTH);
        }
        run.setLogs(updatedLogs);
        nlpRunRepository.save(run);
    }

    private void finish(Long runId, NLPRunStatus status) {
        NLPRun run = nlpRunRepository.findById(runId).orElseThrow();
        run.setStatus(status);
        run.setEndTime(LocalDateTime.now());
        if (run.getStartTime() != null) {
            run.setDurationSeconds(Duration.between(run.getStartTime(), run.getEndTime()).toSeconds());
        }
        MetricsResultDTO metrics = nlpMetricsParser.parse(run.getLogs());
        run.setAccuracy(metrics.accuracy());
        run.setF1Score(metrics.f1Score());
        run.setConfusionMatrix(metrics.confusionMatrix());
        nlpRunRepository.save(run);
    }

    private String extractParam(String json, String key, String fallback) {
        if (json == null) {
            return fallback;
        }
        String needle = "\"" + key + "\":";
        int start = json.indexOf(needle);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + needle.length();
        int valueEnd = json.indexOf(',', valueStart);
        if (valueEnd < 0) {
            valueEnd = json.indexOf('}', valueStart);
        }
        return valueEnd > valueStart ? json.substring(valueStart, valueEnd).replace("\"", "").trim() : fallback;
    }
}
