package ma.perenity.backend.mapper;

import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.entities.ApplicationEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class ApplicationMapperImpl implements ApplicationMapper {

    @Override
    public ApplicationDTO toDto(ApplicationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ApplicationDTO.ApplicationDTOBuilder applicationDTO = ApplicationDTO.builder();

        applicationDTO.id( entity.getId() );
        applicationDTO.code( entity.getCode() );
        applicationDTO.libelle( entity.getLibelle() );
        applicationDTO.description( entity.getDescription() );
        applicationDTO.actif( entity.getActif() );

        return applicationDTO.build();
    }

    @Override
    public ApplicationEntity toEntity(ApplicationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ApplicationEntity.ApplicationEntityBuilder applicationEntity = ApplicationEntity.builder();

        applicationEntity.code( dto.getCode() );
        applicationEntity.libelle( dto.getLibelle() );
        applicationEntity.description( dto.getDescription() );
        applicationEntity.actif( dto.getActif() );

        return applicationEntity.build();
    }

    @Override
    public void updateEntityFromDto(ApplicationDTO dto, ApplicationEntity entity) {
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
