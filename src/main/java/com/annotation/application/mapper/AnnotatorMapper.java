package com.annotation.application.mapper;

import com.annotation.application.dto.AnnotatorDTO;
import com.annotation.domain.entity.Annotator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnnotatorMapper {
    @Mapping(target = "annotationsCount", ignore = true)
    @Mapping(target = "tasksCompleted", ignore = true)
    AnnotatorDTO toDto(Annotator entity);
}
