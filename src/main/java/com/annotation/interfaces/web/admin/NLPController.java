package com.annotation.interfaces.web.admin;

import com.annotation.application.dto.DatasetDTO;
import com.annotation.application.dto.NLPRunLogDTO;
import com.annotation.application.dto.NLPRunRequestDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.service.IDatasetService;
import com.annotation.application.service.INLPService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for launching and monitoring NLP training and test scripts.
 */
@Controller
@RequestMapping("/admin/nlp")
@PreAuthorize("hasRole('ADMIN')")
public class NLPController {

    private final IDatasetService datasetService;
    private final INLPService nlpService;

    public NLPController(IDatasetService datasetService, INLPService nlpService) {
        this.datasetService = datasetService;
        this.nlpService = nlpService;
    }

    /**
     * Displays datasets available for NLP runs.
     *
     * @param model view model
     * @return overview template
     */
    @GetMapping
    public String overview(Model model) {
        PageResponseDTO<DatasetDTO> datasets = datasetService.getAllDatasets(PageRequest.of(0, 100));
        model.addAttribute("datasetsPage", datasets);
        return "admin/nlp/overview";
    }

    /**
     * Displays the launch form for a dataset.
     *
     * @param datasetId dataset ID
     * @param model view model
     * @return launch form template
     */
    @GetMapping("/{datasetId}/new")
    public String launchForm(@PathVariable Long datasetId, Model model) {
        model.addAttribute("dataset", datasetService.getDatasetById(datasetId));
        model.addAttribute("runRequest", new NLPRunRequestDTO(datasetId, 0.001, 10, 32, "train.py"));
        model.addAttribute("history", nlpService.getRunHistory(datasetId));
        return "admin/nlp/launch-form";
    }

    /**
     * Starts an asynchronous training run.
     *
     * @param datasetId dataset ID
     * @param request form request
     * @param bindingResult validation result
     * @param model view model
     * @param redirectAttributes redirect flash attributes
     * @return redirect to run details
     */
    @PostMapping("/{datasetId}/train")
    public String train(@PathVariable Long datasetId,
                        @Valid @ModelAttribute("runRequest") NLPRunRequestDTO request,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("dataset", datasetService.getDatasetById(datasetId));
            return "admin/nlp/launch-form";
        }
        NLPRunRequestDTO normalized = new NLPRunRequestDTO(datasetId, request.learningRate(), request.epochs(), request.batchSize(), request.scriptPath());
        Long runId = nlpService.runTraining(normalized).id();
        redirectAttributes.addFlashAttribute("successMessage", "Training lancé en arrière-plan.");
        return "redirect:/admin/nlp/runs/" + runId;
    }

    /**
     * Starts an asynchronous test run.
     *
     * @param datasetId dataset ID
     * @param request form request
     * @return redirect to run details
     */
    @PostMapping("/{datasetId}/test")
    public String test(@PathVariable Long datasetId,
                       @Valid @ModelAttribute("runRequest") NLPRunRequestDTO request,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("dataset", datasetService.getDatasetById(datasetId));
            return "admin/nlp/launch-form";
        }

        NLPRunRequestDTO normalized = new NLPRunRequestDTO(datasetId, request.learningRate(), request.epochs(), request.batchSize(),
                request.scriptPath() == null || request.scriptPath().isBlank() ? "test.py" : request.scriptPath());
        Long runId = nlpService.runTest(normalized, null).id();
        redirectAttributes.addFlashAttribute("successMessage", "Test lancé en arrière-plan.");
        return "redirect:/admin/nlp/runs/" + runId;
    }

    /**
     * Displays one NLP run and its logs.
     *
     * @param runId run ID
     * @param model view model
     * @return run detail template
     */
    @GetMapping("/runs/{runId}")
    public String runDetail(@PathVariable Long runId, Model model) {
        model.addAttribute("run", nlpService.getRun(runId));
        return "admin/nlp/run-detail";
    }

    /**
     * Polling endpoint returning JSON logs, status and metrics.
     *
     * @param runId run ID
     * @return polling payload
     */
    @GetMapping("/runs/{runId}/logs")
    @ResponseBody
    public NLPRunLogDTO runLogs(@PathVariable Long runId) {
        return nlpService.getRunLogPayload(runId);
    }

    /**
     * Requests cancellation for an active run.
     *
     * @param runId run ID
     * @return run detail redirect
     */
    @PostMapping("/runs/{runId}/cancel")
    public String cancel(@PathVariable Long runId, RedirectAttributes redirectAttributes) {
        nlpService.cancelRun(runId);
        redirectAttributes.addFlashAttribute("successMessage", "Demande d'arrêt envoyée.");
        return "redirect:/admin/nlp/runs/" + runId;
    }
}
