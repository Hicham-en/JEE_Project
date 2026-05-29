package com.annotation.presentation.controller.admin;

import com.annotation.application.dto.DatasetDTO;
import com.annotation.application.dto.MetricsDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.dto.SpammerDTO;
import com.annotation.application.service.IDatasetService;
import com.annotation.application.service.IMetricsService;
import com.annotation.domain.repository.AnnotationRepository;
import com.annotation.domain.entity.Annotation;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

    private final IMetricsService metricsService;
    private final IDatasetService datasetService;
    private final AnnotationRepository annotationRepository;

    public DashboardController(IMetricsService metricsService, IDatasetService datasetService, AnnotationRepository annotationRepository) {
        this.metricsService = metricsService;
        this.datasetService = datasetService;
        this.annotationRepository = annotationRepository;
    }

    @GetMapping
    public String index(Model model) {
        PageResponseDTO<DatasetDTO> datasets = datasetService.getAllDatasets(PageRequest.of(0, 100)); // Limit for UI simplification
        model.addAttribute("datasets", datasets.content());
        return "admin/dashboard/index";
    }

    @GetMapping("/{datasetId}")
    public String datasetMetrics(@PathVariable Long datasetId, Model model) {
        DatasetDTO dataset = datasetService.getDatasetById(datasetId);
        MetricsDTO metrics = metricsService.computeFleissKappa(datasetId);
        List<SpammerDTO> spammers = metricsService.detectSpammers(datasetId);
        double progress = datasetService.calculateProgress(datasetId);

        model.addAttribute("dataset", dataset);
        model.addAttribute("metrics", metrics);
        model.addAttribute("spammers", spammers);
        model.addAttribute("progress", progress);

        // Required by layout if navigating back
        PageResponseDTO<DatasetDTO> datasets = datasetService.getAllDatasets(PageRequest.of(0, 100));
        model.addAttribute("datasets", datasets.content());

        return "admin/dashboard/index";
    }

    @GetMapping("/{datasetId}/export")
    public ResponseEntity<byte[]> exportDatasetToCsv(@PathVariable Long datasetId) {
        DatasetDTO dataset = datasetService.getDatasetById(datasetId);
        List<Annotation> annotations = annotationRepository.findByTaskDatasetId(datasetId);

        StringBuilder csv = new StringBuilder();
        // BOM for Excel compatibility with UTF-8
        csv.append('\ufeff');
        csv.append("ID Annotation,Text 1,Text 2,Annotateur,Classe Choisie,Duree (s),Date Annotation\n");

        for (Annotation ann : annotations) {
            String text1 = ann.getTextPair() != null ? ann.getTextPair().getText1() : "";
            String text2 = ann.getTextPair() != null ? ann.getTextPair().getText2() : "";
            String annotator = ann.getAnnotator() != null ? ann.getAnnotator().getLogin() : "";
            String chosenClass = ann.getChosenClass();
            String duration = ann.getDurationSeconds() != null ? String.valueOf(ann.getDurationSeconds()) : "";
            String date = ann.getAnnotationTime() != null ? ann.getAnnotationTime().toString() : "";

            csv.append(escapeCsv(String.valueOf(ann.getId()))).append(",");
            csv.append(escapeCsv(text1)).append(",");
            csv.append(escapeCsv(text2)).append(",");
            csv.append(escapeCsv(annotator)).append(",");
            csv.append(escapeCsv(chosenClass)).append(",");
            csv.append(escapeCsv(duration)).append(",");
            csv.append(escapeCsv(date)).append("\n");
        }

        byte[] output = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String safeDatasetName = dataset.nom() != null ? dataset.nom().replaceAll("[^a-zA-Z0-9.-]", "_") : "export";
        String filename = String.format("dataset-%s-%s.csv", safeDatasetName, LocalDate.now().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(output);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        boolean containsQuotes = value.contains("\"");
        boolean containsComma = value.contains(",");
        boolean containsNewline = value.contains("\n") || value.contains("\r");

        if (containsQuotes || containsComma || containsNewline) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
