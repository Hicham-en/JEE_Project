package com.annotation.presentation.controller.admin;

import com.annotation.application.dto.AnnotatorCreateDTO;
import com.annotation.application.dto.AnnotatorCreatedDTO;
import com.annotation.application.dto.AnnotatorUpdateDTO;
import com.annotation.application.dto.AnnotatorDTO;
import com.annotation.application.dto.PageResponseDTO;
import com.annotation.application.exception.DuplicateLoginException;
import com.annotation.application.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/annotators")
public class AnnotatorController {

    private final IUserService userService;

    public AnnotatorController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listAnnotators(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponseDTO<AnnotatorDTO> response = userService.getAllAnnotators(pageable);
        model.addAttribute("annotators", response.content());
        model.addAttribute("currentPage", response.pageNumber());
        model.addAttribute("totalPages", response.totalPages());
        return "admin/annotators/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("annotator")) {
            model.addAttribute("annotator", new AnnotatorCreateDTO("", "", ""));
        }
        return "admin/annotators/create";
    }

    @PostMapping("/create")
    public String createAnnotator(
            @Valid @ModelAttribute("annotator") AnnotatorCreateDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/annotators/create";
        }

        try {
            AnnotatorCreatedDTO created = userService.createAnnotator(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Annotateur créé avec succès. Login: " + created.annotator().login());
            redirectAttributes.addFlashAttribute("generatedPassword", created.generatedPassword());
            return "redirect:/admin/annotators";
        } catch (DuplicateLoginException e) {
            redirectAttributes.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/admin/annotators/create-err";
        }
    }

    @RequestMapping(value = "/create-err", method = {RequestMethod.GET, RequestMethod.POST})
    public String createAnnotatorErr(Model model) {
        return "admin/annotators/create";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("annotator")) {
            AnnotatorDTO exist = userService.getAnnotatorById(id);
            model.addAttribute("annotator", new AnnotatorUpdateDTO(exist.nom(), exist.prenom(), exist.login()));
        }
        model.addAttribute("annotatorId", id);
        return "admin/annotators/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAnnotator(
            @PathVariable Long id,
            @Valid @ModelAttribute("annotator") AnnotatorUpdateDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
            
        if (result.hasErrors()) {
            model.addAttribute("annotatorId", id);
            return "admin/annotators/edit";
        }
        
        try {
            userService.updateAnnotator(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Annotateur mis à jour avec succès.");
            return "redirect:/admin/annotators";
        } catch (DuplicateLoginException e) {
            redirectAttributes.addFlashAttribute("loginError", e.getMessage());
            redirectAttributes.addFlashAttribute("annotatorId", id);
            return "redirect:/admin/annotators/" + id + "/edit-err";
        }
    }
    
    @RequestMapping(value = "/{id}/edit-err", method = {RequestMethod.GET, RequestMethod.POST})
    public String editAnnotatorErr(@PathVariable Long id, Model model) {
        model.addAttribute("annotatorId", id);
        return "admin/annotators/edit";
    }

    @PostMapping("/{id}/delete")
    public String deleteAnnotator(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.softDeleteAnnotator(id);
        redirectAttributes.addFlashAttribute("successMessage", "Annotateur désactivé avec succès. Les tâches en cours ont été annulées.");
        return "redirect:/admin/annotators";
    }
}
