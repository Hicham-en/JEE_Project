package com.annotation.interfaces.web.admin;

import com.annotation.application.service.ExportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for downloading dataset annotation exports.
 */
@Controller
@RequestMapping("/admin/datasets")
@PreAuthorize("hasRole('ADMIN')")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Downloads a dataset export in CSV format.
     *
     * @param datasetId dataset ID
     * @return CSV response
     */
    @GetMapping("/{datasetId}/export/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long datasetId) {
        return download("dataset-" + datasetId + ".csv", "text/csv", exportService.exportDatasetCsv(datasetId));
    }

    /**
     * Downloads a dataset export in JSON format.
     *
     * @param datasetId dataset ID
     * @return JSON response
     */
    @GetMapping("/{datasetId}/export/json")
    public ResponseEntity<byte[]> exportJson(@PathVariable Long datasetId) {
        return download("dataset-" + datasetId + ".json", MediaType.APPLICATION_JSON_VALUE, exportService.exportDatasetJson(datasetId));
    }

    private ResponseEntity<byte[]> download(String filename, String contentType, byte[] content) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }
}
