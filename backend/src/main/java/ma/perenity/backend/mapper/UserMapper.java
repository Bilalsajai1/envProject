package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.UserCreateUpdateDTO;
import ma.perenity.backend.dto.UserDTO;
import ma.perenity.backend.entities.UtilisateurEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "profilId", source = "profil.id")
    @Mapping(target = "profilCode", source = "profil.code")
    @Mapping(target = "profilLibelle", source = "profil.libelle")
    UserDTO toDto(UtilisateurEntity entity);

    List<UserDTO> toDtoList(List<UtilisateurEntity> entities);

    // Pour CREATE
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profil", ignore = true) // sera set dans le service via profilRepository
    @Mapping(target = "keycloakId", ignore = true) // si tu gères ça plus tard
    UtilisateurEntity toEntity(UserCreateUpdateDTO dto);

    // Pour UPDATE partiel
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    void updateEntityFromDto(UserCreateUpdateDTO dto, @MappingTarget UtilisateurEntity entity);
}
