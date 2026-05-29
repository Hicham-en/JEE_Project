package com.annotation.application.service.impl;

import com.annotation.application.dto.*;
import com.annotation.application.exception.DuplicateLoginException;
import com.annotation.application.exception.EntityNotFoundException;
import com.annotation.application.mapper.AnnotatorMapper;
import com.annotation.application.service.IUserService;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.entity.Role;
import com.annotation.domain.entity.Task;
import com.annotation.domain.entity.TaskStatus;
import com.annotation.domain.repository.AnnotatorRepository;
import com.annotation.domain.repository.RoleRepository;
import com.annotation.domain.repository.TaskRepository;
import com.annotation.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements IUserService {

    private final AnnotatorRepository annotatorRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final RoleRepository roleRepository;
    private final AnnotatorMapper annotatorMapper;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserServiceImpl(
            AnnotatorRepository annotatorRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            RoleRepository roleRepository,
            AnnotatorMapper annotatorMapper,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager) {
        this.annotatorRepository = annotatorRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.roleRepository = roleRepository;
        this.annotatorMapper = annotatorMapper;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public AnnotatorCreatedDTO createAnnotator(AnnotatorCreateDTO dto) {
        if (userRepository.findByLogin(dto.login()).isPresent()) {
            throw new DuplicateLoginException("Ce login est déjà utilisé.");
        }

        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role role = roleRepository.findByNomRole("ANNOTATOR_ROLE")
            .orElseThrow(() -> new RuntimeException("Rôle ANNOTATOR_ROLE introuvable en base de données"));

        Annotator annotator = new Annotator();
        annotator.setNom(dto.nom());
        annotator.setPrenom(dto.prenom());
        annotator.setLogin(dto.login());
        annotator.setPasswordHash(encodedPassword);
        annotator.setActive(true);

        if (annotator.getRoles() == null) {
            annotator.setRoles(new HashSet<>());
        }
        annotator.getRoles().add(role);

        entityManager.persist(annotator);
        entityManager.flush();

        AnnotatorDTO outDto = annotatorMapper.toDto(annotator);
        return new AnnotatorCreatedDTO(outDto, rawPassword);
    }

    @Override
    @Transactional
    public AnnotatorDTO updateAnnotator(Long id, AnnotatorUpdateDTO dto) {
        Annotator annotator = annotatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotator non trouvé : " + id));

        if (!annotator.getLogin().equals(dto.login()) && userRepository.findByLogin(dto.login()).isPresent()) {
            throw new DuplicateLoginException("Ce login est déjà utilisé.");
        }

        annotator.setNom(dto.nom());
        annotator.setPrenom(dto.prenom());
        annotator.setLogin(dto.login());

        return annotatorMapper.toDto(annotatorRepository.save(annotator));
    }

    @Override
    @Transactional
    public void softDeleteAnnotator(Long id) {
        Annotator annotator = annotatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotator non trouvé : " + id));
        annotator.setActive(false);
        annotatorRepository.save(annotator);

        List<Task> pendingTasks = taskRepository.findByAnnotatorAndStatusIn(
                annotator, List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS));
        
        for (Task task : pendingTasks) {
            task.setStatus(TaskStatus.CANCELLED);
        }
        taskRepository.saveAll(pendingTasks);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AnnotatorDTO> getAllAnnotators(Pageable pageable) {
        Page<Annotator> page = annotatorRepository.findAll(pageable);
        List<AnnotatorDTO> list = page.getContent().stream()
                .map(annotatorMapper::toDto)
                .toList();

        return new PageResponseDTO<>(
            list,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AnnotatorDTO getAnnotatorById(Long id) {
        Annotator annotator = annotatorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotator non trouvé : " + id));
        return annotatorMapper.toDto(annotator);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
