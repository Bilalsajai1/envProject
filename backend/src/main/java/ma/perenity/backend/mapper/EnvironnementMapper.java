package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EnvironnementMapper {

    @Mapping(target = "projetId", source = "projet.id")
    @Mapping(target = "typeCode", source = "type.code")
    EnvironnementDTO toDto(EnvironnementEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projet", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "envApplications", ignore = true)
    @Mapping(target = "roles", ignore = true)
    EnvironnementEntity toEntity(EnvironnementDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projet", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "envApplications", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateEntityFromDto(EnvironnementDTO dto, @MappingTarget EnvironnementEntity entity);
}
