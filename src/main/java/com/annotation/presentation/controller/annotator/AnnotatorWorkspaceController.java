package com.annotation.presentation.controller.annotator;

import com.annotation.application.dto.AnnotationCreateDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.dto.TaskDTO;
import com.annotation.application.service.IAnnotationService;
import com.annotation.domain.entity.TextPair;
import com.annotation.domain.repository.TaskRepository;
import com.annotation.domain.repository.TextPairRepository;
import com.annotation.domain.repository.UserRepository;
import com.annotation.domain.entity.User;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.Dataset;
import com.annotation.domain.repository.AnnotationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Controller
@RequestMapping("/annotator/workspace")
public class AnnotatorWorkspaceController {

    private final IAnnotationService annotationService;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TextPairRepository textPairRepository;
    private final AnnotationRepository annotationRepository;

    public AnnotatorWorkspaceController(IAnnotationService annotationService, UserRepository userRepository,
                                        TaskRepository taskRepository, TextPairRepository textPairRepository,
                                        AnnotationRepository annotationRepository) {
        this.annotationService = annotationService;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.textPairRepository = textPairRepository;
        this.annotationRepository = annotationRepository;
    }

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String listTasks(Model model, Authentication authentication,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        Long annotatorId = getUserId(authentication);
        PageResponseDTO<TaskDTO> tasks = annotationService.getTasksForAnnotator(annotatorId, PageRequest.of(page, size));
        model.addAttribute("tasks", tasks);
        return "annotator/workspace/list";
    }

    @GetMapping("/task/{taskId}")
    @Transactional(readOnly = true)
    public String solveTask(@PathVariable Long taskId, Authentication authentication, Model model) {
        Long annotatorId = getUserId(authentication);
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (!task.getAnnotator().getId().equals(annotatorId)) {
            return "redirect:/annotator/workspace";
        }

        // Forcer l'initialisation des associations lazy tant que la transaction est ouverte
        task.getAnnotator().getNom();
        Dataset dataset = task.getDataset();
        dataset.getPossibleClasses().size();
        dataset.getNom();

        List<TextPair> textPairs = task.getTextPairs();
        model.addAttribute("task", task);
        model.addAttribute("dataset", dataset);
        model.addAttribute("textPairs", textPairs);

        List<com.annotation.domain.entity.Annotation> existingAnnotations = annotationRepository.findByTaskDatasetId(dataset.getId());
        java.util.Map<Long, String> userAnnotations = existingAnnotations.stream()
                .filter(a -> a.getAnnotator().getId().equals(annotatorId))
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getTextPair().getId(), 
                        com.annotation.domain.entity.Annotation::getChosenClass,
                        (existing, replacementVal) -> existing
                ));
        model.addAttribute("userAnnotations", userAnnotations);


        return "annotator/workspace/solve";
    }

    @PostMapping("/task/{taskId}/annotate")
    public String submitAnnotation(@PathVariable Long taskId,
                                   @RequestParam Long textPairId,
                                   @RequestParam String chosenClass,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        Long annotatorId = getUserId(authentication);
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (!task.getAnnotator().getId().equals(annotatorId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Action non autorisée pour cette tâche.");
            return "redirect:/annotator/workspace";
        }

        AnnotationCreateDTO dto = new AnnotationCreateDTO(taskId, textPairId, chosenClass);
        annotationService.saveAnnotation(dto, annotatorId);
        redirectAttributes.addFlashAttribute("successMessage", "Annotation saisie avec succès !");

        return redirectAfterSubmission(task, redirectAttributes);
    }

    @PostMapping("/task/{taskId}/annotate-all")
    @Transactional
    public String submitAllAnnotations(@PathVariable Long taskId,
                                       @RequestParam(value = "singleTextPairId", required = false) Long singleTextPairId,
                                       HttpServletRequest request,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        Long annotatorId = getUserId(authentication);
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (!task.getAnnotator().getId().equals(annotatorId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Action non autorisée pour cette tâche.");
            return "redirect:/annotator/workspace";
        }

        // Forcer l'initialisation des textPairs dans la transaction
        int totalPairs = task.getTextPairs().size();

        if (singleTextPairId != null) {
            String chosenClass = request.getParameter("chosenClassByTextPairId[" + singleTextPairId + "]");
            if (chosenClass == null || chosenClass.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Veuillez choisir une classe avant de valider cette ligne.");
                return "redirect:/annotator/workspace/task/" + taskId;
            }

            AnnotationCreateDTO dto = new AnnotationCreateDTO(taskId, singleTextPairId, chosenClass);
            annotationService.saveAnnotation(dto, annotatorId);
            redirectAttributes.addFlashAttribute("successMessage", "Annotation saisie avec succès !");
            return redirectAfterSubmission(task, redirectAttributes);
        }

        int savedCount = 0;
        for (TextPair textPair : task.getTextPairs()) {
            Long textPairId = textPair.getId();
            String chosenClass = request.getParameter("chosenClassByTextPairId[" + textPairId + "]");
            if (chosenClass == null || chosenClass.isBlank()) {
                continue;
            }
            AnnotationCreateDTO dto = new AnnotationCreateDTO(taskId, textPairId, chosenClass);
            annotationService.saveAnnotation(dto, annotatorId);
            savedCount++;
        }

        if (savedCount == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Aucune annotation valide n'a été soumise.");
            return "redirect:/annotator/workspace/task/" + taskId;
        }

        if (savedCount >= totalPairs) {
            redirectAttributes.addFlashAttribute("successMessage", "Tâche terminée ! Merci pour vos annotations.");
            return "redirect:/annotator/workspace";
        }

        redirectAttributes.addFlashAttribute("successMessage", savedCount + " annotations enregistrées avec succès !");
        return "redirect:/annotator/workspace/task/" + task.getId();
    }

    private String redirectAfterSubmission(Task task, RedirectAttributes redirectAttributes) {
        long totalPairs = task.getTextPairs().size();
        long annotatedCount = annotationRepository.countByTaskId(task.getId());

        if (annotatedCount >= totalPairs) {
            redirectAttributes.addFlashAttribute("successMessage", "Tâche terminée ! Merci pour vos annotations.");
            return "redirect:/annotator/workspace";
        }

        return "redirect:/annotator/workspace/task/" + task.getId();
    }
}
