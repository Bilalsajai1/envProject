package ma.perenity.backend.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.ProfilDTO;
import ma.perenity.backend.entities.ProfilEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class ProfilMapperImpl implements ProfilMapper {

    @Override
    public ProfilDTO toDto(ProfilEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ProfilDTO profilDTO = new ProfilDTO();

        profilDTO.setId( entity.getId() );
        profilDTO.setCode( entity.getCode() );
        profilDTO.setLibelle( entity.getLibelle() );
        profilDTO.setDescription( entity.getDescription() );
        profilDTO.setAdmin( entity.getAdmin() );
        profilDTO.setActif( entity.getActif() );

        profilDTO.setNbUsers( entity.getUtilisateurs() != null ? entity.getUtilisateurs().size() : 0 );

        return profilDTO;
    }

    @Override
    public List<ProfilDTO> toDtoList(List<ProfilEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ProfilDTO> list = new ArrayList<ProfilDTO>( entities.size() );
        for ( ProfilEntity profilEntity : entities ) {
            list.add( toDto( profilEntity ) );
        }

        return list;
    }
}
