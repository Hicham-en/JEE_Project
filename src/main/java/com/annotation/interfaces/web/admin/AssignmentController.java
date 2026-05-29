package com.annotation.interfaces.web.admin;

import com.annotation.application.dto.AssignmentResultDTO;
import com.annotation.application.dto.DatasetDTO;
import com.annotation.application.exception.AssignmentException;
import com.annotation.application.service.IAssignmentService;
import com.annotation.application.service.IDatasetService;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.TaskStatus;
import com.annotation.domain.repository.AnnotatorRepository;
import com.annotation.domain.repository.TaskRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/datasets/{id}")
@PreAuthorize("hasRole('ADMIN')")
public class AssignmentController {

    private final IAssignmentService assignmentService;
    private final IDatasetService datasetService;
    private final AnnotatorRepository annotatorRepository;
    private final TaskRepository taskRepository;

    public AssignmentController(IAssignmentService assignmentService,
                                IDatasetService datasetService,
                                AnnotatorRepository annotatorRepository,
                                TaskRepository taskRepository) {
        this.assignmentService = assignmentService;
        this.datasetService = datasetService;
        this.annotatorRepository = annotatorRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/assign")
    public String showAssignPage(@PathVariable("id") Long datasetId, Model model) {
        DatasetDTO dataset = datasetService.getDatasetById(datasetId);
        List<Annotator> activeAnnotators = annotatorRepository.findByActiveTrue();
        
        // Find already assigned (not cancelled) 
        List<Task> tasks = taskRepository.findByDatasetId(datasetId);
        Set<Long> alreadyAssignedIds = tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.CANCELLED)
                .map(t -> t.getAnnotator().getId())
                .collect(Collectors.toSet());

        model.addAttribute("dataset", dataset);
        model.addAttribute("annotators", activeAnnotators);
        model.addAttribute("assignedIds", alreadyAssignedIds);

        return "admin/datasets/assign";
    }

    @PostMapping("/assign")
    public String assignAnnotators(@PathVariable("id") Long datasetId,
                                   @RequestParam(value = "annotatorIds", required = false) List<Long> annotatorIds,
                                   RedirectAttributes redirectAttributes) {
        try {
            AssignmentResultDTO result = assignmentService.assignAnnotators(datasetId, annotatorIds);
            String msg = String.format("Assignation réussie : %d annotateurs assignés, %d nouvelles tâches créées (~%d textes/annotateur)",
                    result.nbAnnotators(), result.nbTachesCreees(), result.nbTextesParAnnotateur());
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (AssignmentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/datasets/" + datasetId + "/assign";
        }
        
        return "redirect:/admin/datasets/" + datasetId;
    }

    @PostMapping("/unassign/{annotatorId}")
    public String unassignAnnotator(@PathVariable("id") Long datasetId,
                                    @PathVariable("annotatorId") Long annotatorId,
                                    RedirectAttributes redirectAttributes) {
        boolean success = assignmentService.unassignAnnotator(datasetId, annotatorId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Annotateur désaffecté avec succès (statut CANCELLED)");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de désaffecter l'annotateur");
        }
        return "redirect:/admin/datasets/" + datasetId;
    }
}
