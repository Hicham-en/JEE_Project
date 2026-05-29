package com.annotation.interfaces.web.admin;

import com.annotation.application.dto.DatasetCreateDTO;
import com.annotation.application.dto.DatasetDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.exception.DatasetImportException;
import com.annotation.application.service.IDatasetService;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.TaskRepository;
import com.annotation.domain.repository.TextPairRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Admin Dataset operations.
 */
@Controller
@RequestMapping("/admin/datasets")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDatasetController {

    private final IDatasetService datasetService;
    private final TaskRepository taskRepository;
    private final TextPairRepository textPairRepository;

    public AdminDatasetController(IDatasetService datasetService, TaskRepository taskRepository,
                                  TextPairRepository textPairRepository) {
        this.datasetService = datasetService;
        this.taskRepository = taskRepository;
        this.textPairRepository = textPairRepository;
    }

    @GetMapping
    public String listDatasets(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponseDTO<DatasetDTO> datasets = datasetService.getAllDatasets(pageable);
        model.addAttribute("datasetsPage", datasets);
        return "admin/datasets/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("datasetDTO", new DatasetCreateDTO("", "", "", ""));
        return "admin/datasets/create";
    }

    @PostMapping("/create")
    public String createDataset(@Valid @ModelAttribute("datasetDTO") DatasetCreateDTO dto,
                                BindingResult bindingResult,
                                @RequestParam(value = "file", required = false) MultipartFile file,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/datasets/create";
        }

        if (file == null || file.isEmpty()) {
            model.addAttribute("fileError", "Veuillez sélectionner un fichier (CSV ou JSON).");
            return "admin/datasets/create";
        }

        try {
            datasetService.createDataset(dto, file);
            redirectAttributes.addFlashAttribute("successMessage", "Dataset créé avec succès");
            return "redirect:/admin/datasets";
        } catch (DatasetImportException e) {
            model.addAttribute("fileError", "Erreur d'importation : " + e.getMessage());
            return "admin/datasets/create";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("fileError", "Un dataset avec ce nom existe déjà.");
            return "admin/datasets/create";
        } catch (Exception e) {
            model.addAttribute("fileError", "Une erreur inattendue s'est produite.");
            return "admin/datasets/create";
        }
    }

    @GetMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String datasetDetail(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size,
                                Model model) {
        DatasetDTO dataset = datasetService.getDatasetById(id);
        List<Annotator> assignedAnnotators = taskRepository.findByDatasetId(id).stream()
                .map(Task::getAnnotator)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Annotator::getId, annotator -> annotator, (left, right) -> left, java.util.LinkedHashMap::new),
                        map -> List.copyOf(map.values())));

        Page<TextPair> textPairsPage = textPairRepository.findByDatasetId(id, PageRequest.of(page, size));
        model.addAttribute("dataset", dataset);
        model.addAttribute("assignedAnnotators", assignedAnnotators);
        model.addAttribute("textPairsPage", textPairsPage);
        return "admin/datasets/detail";
    }
}
