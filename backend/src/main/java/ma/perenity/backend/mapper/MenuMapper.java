package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.MenuDTO;
import ma.perenity.backend.dto.MenuCreateUpdateDTO;
import ma.perenity.backend.entities.MenuEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentCode", source = "parent.code")
    @Mapping(target = "environmentTypeId", source = "environmentType.id")
    @Mapping(target = "environmentTypeCode", source = "environmentType.code")
    MenuDTO toDTO(MenuEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "environmentType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    MenuEntity toEntity(MenuCreateUpdateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "environmentType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateEntity(MenuCreateUpdateDTO dto, @MappingTarget MenuEntity entity);
}
