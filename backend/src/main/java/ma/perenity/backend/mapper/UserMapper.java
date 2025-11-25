package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.UserDTO;
import ma.perenity.backend.entities.UtilisateurEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "profilId", source = "profil.id")
    @Mapping(target = "profilCode", source = "profil.code")
    @Mapping(target = "profilLibelle", source = "profil.libelle")
    UserDTO toDto(UtilisateurEntity entity);

    List<UserDTO> toDtoList(List<UtilisateurEntity> entities);
}
