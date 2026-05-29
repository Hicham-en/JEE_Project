package com.annotation.interfaces.web.admin;

import com.annotation.application.dto.DatasetDTO;
import com.annotation.application.dto.MetricsDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.dto.SpammerDTO;
import com.annotation.application.service.IDatasetService;
import com.annotation.application.service.IMetricsService;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.repository.AnnotationRepository;
import com.annotation.domain.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for agreement metrics and spammer detection screens.
 */
@Controller
@RequestMapping("/admin/metrics")
@PreAuthorize("hasRole('ADMIN')")
public class MetricsController {

    private final IDatasetService datasetService;
    private final IMetricsService metricsService;
    private final TaskRepository taskRepository;
    private final AnnotationRepository annotationRepository;

    public MetricsController(IDatasetService datasetService,
                             IMetricsService metricsService,
                             TaskRepository taskRepository,
                             AnnotationRepository annotationRepository) {
        this.datasetService = datasetService;
        this.metricsService = metricsService;
        this.taskRepository = taskRepository;
        this.annotationRepository = annotationRepository;
    }

    /**
     * Displays datasets with metric actions.
     *
     * @param model view model
     * @return metrics overview template
     */
    @GetMapping
    public String overview(Model model) {
        PageResponseDTO<DatasetDTO> datasets = datasetService.getAllDatasets(PageRequest.of(0, 100));
        model.addAttribute("datasetsPage", datasets);
        return "admin/metrics/overview";
    }

    /**
     * Displays Fleiss Kappa and Cohen Kappa pairs for a dataset.
     *
     * @param datasetId dataset ID
     * @param model view model
     * @return quality template
     */
    @GetMapping("/{datasetId}/quality")
    @Transactional(readOnly = true)
    public String quality(@PathVariable Long datasetId, Model model) {
        DatasetDTO dataset = datasetService.getDatasetById(datasetId);
        long annotationCount = annotationRepository.countByTaskDatasetId(datasetId);
        if (annotationCount == 0) {
            model.addAttribute("dataset", dataset);
            model.addAttribute("metrics", new MetricsDTO(datasetId, 0.0, 0, 0, "Pas d'annotations"));
            model.addAttribute("cohenPairs", List.of());
            model.addAttribute("warningMessage", "Ce dataset ne contient encore aucune annotation. Les métriques seront disponibles une fois que les annotateurs auront commencé.");
            return "admin/metrics/quality";
        }
        MetricsDTO metrics = metricsService.computeFleissKappa(datasetId);
        List<Map<String, Object>> pairs = cohenPairs(datasetId);
        model.addAttribute("dataset", dataset);
        model.addAttribute("metrics", metrics);
        model.addAttribute("cohenPairs", pairs);
        return "admin/metrics/quality";
    }

    /**
     * Displays spammer detection results for a dataset.
     *
     * @param datasetId dataset ID
     * @param model view model
     * @return spammers template
     */
    @GetMapping("/{datasetId}/spammers")
    public String spammers(@PathVariable Long datasetId, Model model) {
        DatasetDTO dataset = datasetService.getDatasetById(datasetId);
        List<SpammerDTO> spammers = metricsService.detectSpammers(datasetId);
        model.addAttribute("dataset", dataset);
        model.addAttribute("spammers", spammers);
        return "admin/metrics/spammers";
    }

    private List<Map<String, Object>> cohenPairs(Long datasetId) {
        List<Annotator> annotators = taskRepository.findByDatasetId(datasetId).stream()
                .map(task -> task.getAnnotator())
                .distinct()
                .sorted(Comparator.comparing(Annotator::getId))
                .toList();
        List<Map<String, Object>> pairs = new ArrayList<>();
        for (int i = 0; i < annotators.size(); i++) {
            for (int j = i + 1; j < annotators.size(); j++) {
                Annotator first = annotators.get(i);
                Annotator second = annotators.get(j);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("first", first.getPrenom() + " " + first.getNom());
                row.put("second", second.getPrenom() + " " + second.getNom());
                row.put("kappa", metricsService.computeCohenKappa(datasetId, first.getId(), second.getId()));
                pairs.add(row);
            }
        }
        return pairs;
    }
}
