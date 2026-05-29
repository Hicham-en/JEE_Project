package com.annotation.application.mapper;

import com.annotation.application.dto.TaskDTO;
import com.annotation.domain.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Task entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    @Mapping(target = "datasetId", source = "dataset.id")
    @Mapping(target = "datasetName", source = "dataset.nom")
    @Mapping(target = "annotatorId", source = "annotator.id")
    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "nbTotal", expression = "java(entity.getTextPairs() != null ? entity.getTextPairs().size() : 0)")
    @Mapping(target = "nbAnnotated", expression = "java(entity.getAnnotations() != null ? entity.getAnnotations().size() : 0)")
    @Mapping(target = "progressPercent", expression = "java(progress(entity))")
    TaskDTO toDto(Task entity);

    @Mapping(target = "dataset", ignore = true)
    @Mapping(target = "annotator", ignore = true)
    @Mapping(target = "annotations", ignore = true)
    @Mapping(target = "textPairs", ignore = true)
    @Mapping(target = "status", ignore = true)
    Task toEntity(TaskDTO dto);

    /**
     * Computes task progress from mapped relations.
     *
     * @param entity task entity
     * @return progress percentage
     */
    default Double progress(Task entity) {
        int total = entity.getTextPairs() != null ? entity.getTextPairs().size() : 0;
        int annotated = entity.getAnnotations() != null ? entity.getAnnotations().size() : 0;
        return total == 0 ? 0.0 : Math.min(100.0, (annotated * 100.0) / total);
    }
}
