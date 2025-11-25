package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.ProfilDTO;
import ma.perenity.backend.entities.ProfilEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfilMapper {

    @Mapping(target = "nbUsers", expression = "java(entity.getUtilisateurs() != null ? entity.getUtilisateurs().size() : 0)")
    ProfilDTO toDto(ProfilEntity entity);

    List<ProfilDTO> toDtoList(List<ProfilEntity> entities);
}
