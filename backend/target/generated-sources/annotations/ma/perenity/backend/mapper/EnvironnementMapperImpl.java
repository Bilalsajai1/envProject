package ma.perenity.backend.mapper;

import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class EnvironnementMapperImpl implements EnvironnementMapper {

    @Override
    public EnvironnementDTO toDto(EnvironnementEntity entity) {
        if ( entity == null ) {
            return null;
        }

        EnvironnementDTO.EnvironnementDTOBuilder environnementDTO = EnvironnementDTO.builder();

        environnementDTO.projetId( entityProjetId( entity ) );
        environnementDTO.typeCode( entityTypeCode( entity ) );
        environnementDTO.id( entity.getId() );
        environnementDTO.code( entity.getCode() );
        environnementDTO.libelle( entity.getLibelle() );
        environnementDTO.description( entity.getDescription() );
        environnementDTO.actif( entity.getActif() );

        return environnementDTO.build();
    }

    @Override
    public EnvironnementEntity toEntity(EnvironnementDTO dto) {
        if ( dto == null ) {
            return null;
        }

        EnvironnementEntity.EnvironnementEntityBuilder environnementEntity = EnvironnementEntity.builder();

        environnementEntity.code( dto.getCode() );
        environnementEntity.libelle( dto.getLibelle() );
        environnementEntity.description( dto.getDescription() );
        environnementEntity.actif( dto.getActif() );

        return environnementEntity.build();
    }

    @Override
    public void updateEntityFromDto(EnvironnementDTO dto, EnvironnementEntity entity) {
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

    private Long entityProjetId(EnvironnementEntity environnementEntity) {
        if ( environnementEntity == null ) {
            return null;
        }
        ProjetEntity projet = environnementEntity.getProjet();
        if ( projet == null ) {
            return null;
        }
        Long id = projet.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityTypeCode(EnvironnementEntity environnementEntity) {
        if ( environnementEntity == null ) {
            return null;
        }
        EnvironmentTypeEntity type = environnementEntity.getType();
        if ( type == null ) {
            return null;
        }
        String code = type.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }
}
