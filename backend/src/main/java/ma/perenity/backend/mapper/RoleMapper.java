package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.RoleDTO;
import ma.perenity.backend.entities.RoleEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "menuId", source = "menu.id")
    @Mapping(target = "menuCode", source = "menu.code")
    @Mapping(target = "environnementId", source = "environnement.id")
    @Mapping(target = "environnementCode", source = "environnement.code")
    @Mapping(target = "projetId", source = "projet.id")
    @Mapping(target = "projetCode", source = "projet.code")
    @Mapping(target = "action", expression = "java(role.getAction() != null ? role.getAction().name() : null)")
    RoleDTO toDto(RoleEntity role);

    List<RoleDTO> toDtoList(List<RoleEntity> roles);
}
