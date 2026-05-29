package com.annotation.application.mapper;

import com.annotation.application.dto.AnnotationDTO;
import com.annotation.domain.entity.Annotation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Annotation entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnnotationMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "textPairId", source = "textPair.id")
    AnnotationDTO toDto(Annotation entity);

    @Mapping(target = "task", ignore = true)
    @Mapping(target = "textPair", ignore = true)
    @Mapping(target = "annotator", ignore = true)
    @Mapping(target = "durationSeconds", ignore = true)
    Annotation toEntity(AnnotationDTO dto);
}
