package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.entities.EnvApplicationEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EnvApplicationMapper {

    @Mapping(target = "environnementId", source = "environnement.id")
    @Mapping(target = "applicationId", source = "application.id")
    @Mapping(target = "applicationCode", source = "application.code")
    @Mapping(target = "applicationLibelle", source = "application.libelle")
    @Mapping(target = "passwordMasked", source = "password")
    EnvApplicationDTO toDto(EnvApplicationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnement", ignore = true)
    @Mapping(target = "application", ignore = true)
    EnvApplicationEntity toEntity(EnvApplicationDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnement", ignore = true)
    @Mapping(target = "application", ignore = true)
    void updateEntity(@MappingTarget EnvApplicationEntity entity, EnvApplicationDTO dto);
}
