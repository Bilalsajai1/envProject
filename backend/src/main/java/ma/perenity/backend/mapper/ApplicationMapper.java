package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.entities.ApplicationEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ApplicationDTO toDto(ApplicationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "envApplications", ignore = true)
    ApplicationEntity toEntity(ApplicationDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "envApplications", ignore = true)
    void updateEntityFromDto(ApplicationDTO dto, @MappingTarget ApplicationEntity entity);
}
