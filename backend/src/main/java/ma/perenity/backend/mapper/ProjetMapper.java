package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.entities.ProjetEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProjetMapper {

    ProjetDTO toDto(ProjetEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnements", ignore = true)
    ProjetEntity toEntity(ProjetDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "environnements", ignore = true)
    void updateEntityFromDto(ProjetDTO dto, @MappingTarget ProjetEntity entity);
}
