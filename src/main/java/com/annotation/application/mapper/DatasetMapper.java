package com.annotation.application.mapper;

import com.annotation.application.dto.DatasetDTO;
import com.annotation.domain.entity.Dataset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Dataset entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatasetMapper {

    @Mapping(target = "progressPercent", ignore = true)
    DatasetDTO toDto(Dataset entity);

    @Mapping(target = "possibleClasses", ignore = true)
    @Mapping(target = "textPairs", ignore = true)
    Dataset toEntity(DatasetDTO dto);
}
