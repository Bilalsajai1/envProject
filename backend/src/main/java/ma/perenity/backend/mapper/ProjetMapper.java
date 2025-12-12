package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.entities.ProjetEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjetMapper {

    @Mapping(target = "envTypeCodes",
            expression = "java(entity.getEnvironmentTypes() == null ? java.util.Collections.emptyList() : " +
                    "entity.getEnvironmentTypes().stream().map(t -> t.getCode()).collect(java.util.stream.Collectors.toList()))")
    ProjetDTO toDto(ProjetEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnements", ignore = true)
    @Mapping(target = "environmentTypes", ignore = true)
    ProjetEntity toEntity(ProjetDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnements", ignore = true)
    @Mapping(target = "environmentTypes", ignore = true)
    void updateEntityFromDto(ProjetDTO dto, @MappingTarget ProjetEntity entity);
}
