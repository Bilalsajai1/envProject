package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EnvironmentTypeMapper {

    EnvironmentTypeDTO toDto(EnvironmentTypeEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnements", ignore = true)
    @Mapping(target = "menus", ignore = true)
    EnvironmentTypeEntity toEntity(EnvironmentTypeDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnements", ignore = true)
    @Mapping(target = "menus", ignore = true)
    void updateEntityFromDto(EnvironmentTypeDTO dto, @MappingTarget EnvironmentTypeEntity entity);
}
