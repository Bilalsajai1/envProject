package ma.perenity.backend.mapper;

import ma.perenity.backend.dto.ProfilDTO;
import ma.perenity.backend.entities.ProfilEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfilMapper {

    @Mapping(target = "nbUsers", expression = "java(countActiveUsers(entity))")
    @Mapping(target = "nbActiveUsers", expression = "java(countActiveUsers(entity))")
    ProfilDTO toDto(ProfilEntity entity);

    @Mapping(target = "nbUsers", source = "userCount")
    @Mapping(target = "nbActiveUsers", source = "userCount")
    ProfilDTO toDto(ProfilEntity entity, int userCount);

    List<ProfilDTO> toDtoList(List<ProfilEntity> entities);

    default int countActiveUsers(ProfilEntity entity) {
        if (entity == null || entity.getUtilisateurs() == null) return 0;
        return (int) entity.getUtilisateurs().stream()
                .filter(u -> u != null && Boolean.TRUE.equals(u.getActif()) && Boolean.FALSE.equals(u.getIsDeleted()))
                .count();
    }
}
