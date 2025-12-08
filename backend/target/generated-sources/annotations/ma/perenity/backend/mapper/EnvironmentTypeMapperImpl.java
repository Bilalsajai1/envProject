package ma.perenity.backend.mapper;

import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class EnvironmentTypeMapperImpl implements EnvironmentTypeMapper {

    @Override
    public EnvironmentTypeDTO toDto(EnvironmentTypeEntity entity) {
        if ( entity == null ) {
            return null;
        }

        EnvironmentTypeDTO.EnvironmentTypeDTOBuilder environmentTypeDTO = EnvironmentTypeDTO.builder();

        environmentTypeDTO.id( entity.getId() );
        environmentTypeDTO.code( entity.getCode() );
        environmentTypeDTO.libelle( entity.getLibelle() );
        environmentTypeDTO.actif( entity.getActif() );

        return environmentTypeDTO.build();
    }

    @Override
    public EnvironmentTypeEntity toEntity(EnvironmentTypeDTO dto) {
        if ( dto == null ) {
            return null;
        }

        EnvironmentTypeEntity.EnvironmentTypeEntityBuilder environmentTypeEntity = EnvironmentTypeEntity.builder();

        environmentTypeEntity.code( dto.getCode() );
        environmentTypeEntity.libelle( dto.getLibelle() );
        environmentTypeEntity.actif( dto.getActif() );

        return environmentTypeEntity.build();
    }

    @Override
    public void updateEntityFromDto(EnvironmentTypeDTO dto, EnvironmentTypeEntity entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getCode() != null ) {
            entity.setCode( dto.getCode() );
        }
        if ( dto.getLibelle() != null ) {
            entity.setLibelle( dto.getLibelle() );
        }
        if ( dto.getActif() != null ) {
            entity.setActif( dto.getActif() );
        }
    }
}
