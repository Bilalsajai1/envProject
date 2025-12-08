package ma.perenity.backend.mapper;

import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.entities.ProjetEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class ProjetMapperImpl implements ProjetMapper {

    @Override
    public ProjetDTO toDto(ProjetEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ProjetDTO.ProjetDTOBuilder projetDTO = ProjetDTO.builder();

        projetDTO.id( entity.getId() );
        projetDTO.code( entity.getCode() );
        projetDTO.libelle( entity.getLibelle() );
        projetDTO.description( entity.getDescription() );
        projetDTO.actif( entity.getActif() );

        return projetDTO.build();
    }

    @Override
    public ProjetEntity toEntity(ProjetDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ProjetEntity.ProjetEntityBuilder projetEntity = ProjetEntity.builder();

        projetEntity.code( dto.getCode() );
        projetEntity.libelle( dto.getLibelle() );
        projetEntity.description( dto.getDescription() );
        projetEntity.actif( dto.getActif() );

        return projetEntity.build();
    }

    @Override
    public void updateEntityFromDto(ProjetDTO dto, ProjetEntity entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getCode() != null ) {
            entity.setCode( dto.getCode() );
        }
        if ( dto.getLibelle() != null ) {
            entity.setLibelle( dto.getLibelle() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getActif() != null ) {
            entity.setActif( dto.getActif() );
        }
    }
}
