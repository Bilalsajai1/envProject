package ma.perenity.backend.mapper;

import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.entities.ApplicationEntity;
import ma.perenity.backend.entities.EnvApplicationEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class EnvApplicationMapperImpl implements EnvApplicationMapper {

    @Override
    public EnvApplicationDTO toDto(EnvApplicationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        EnvApplicationDTO.EnvApplicationDTOBuilder envApplicationDTO = EnvApplicationDTO.builder();

        envApplicationDTO.environnementId( entityEnvironnementId( entity ) );
        envApplicationDTO.applicationId( entityApplicationId( entity ) );
        envApplicationDTO.applicationCode( entityApplicationCode( entity ) );
        envApplicationDTO.applicationLibelle( entityApplicationLibelle( entity ) );
        envApplicationDTO.passwordMasked( entity.getPassword() );
        envApplicationDTO.id( entity.getId() );
        envApplicationDTO.protocole( entity.getProtocole() );
        envApplicationDTO.host( entity.getHost() );
        envApplicationDTO.port( entity.getPort() );
        envApplicationDTO.url( entity.getUrl() );
        envApplicationDTO.username( entity.getUsername() );
        envApplicationDTO.password( entity.getPassword() );
        envApplicationDTO.description( entity.getDescription() );
        envApplicationDTO.actif( entity.getActif() );
        envApplicationDTO.dateDerniereLivraison( entity.getDateDerniereLivraison() );

        return envApplicationDTO.build();
    }

    @Override
    public EnvApplicationEntity toEntity(EnvApplicationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        EnvApplicationEntity.EnvApplicationEntityBuilder envApplicationEntity = EnvApplicationEntity.builder();

        envApplicationEntity.protocole( dto.getProtocole() );
        envApplicationEntity.host( dto.getHost() );
        envApplicationEntity.port( dto.getPort() );
        envApplicationEntity.username( dto.getUsername() );
        envApplicationEntity.password( dto.getPassword() );
        envApplicationEntity.url( dto.getUrl() );
        envApplicationEntity.dateDerniereLivraison( dto.getDateDerniereLivraison() );
        envApplicationEntity.description( dto.getDescription() );
        envApplicationEntity.actif( dto.getActif() );

        return envApplicationEntity.build();
    }

    @Override
    public void updateEntityFromDto(EnvApplicationDTO dto, EnvApplicationEntity entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getProtocole() != null ) {
            entity.setProtocole( dto.getProtocole() );
        }
        if ( dto.getHost() != null ) {
            entity.setHost( dto.getHost() );
        }
        if ( dto.getPort() != null ) {
            entity.setPort( dto.getPort() );
        }
        if ( dto.getUsername() != null ) {
            entity.setUsername( dto.getUsername() );
        }
        if ( dto.getPassword() != null ) {
            entity.setPassword( dto.getPassword() );
        }
        if ( dto.getUrl() != null ) {
            entity.setUrl( dto.getUrl() );
        }
        if ( dto.getDateDerniereLivraison() != null ) {
            entity.setDateDerniereLivraison( dto.getDateDerniereLivraison() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getActif() != null ) {
            entity.setActif( dto.getActif() );
        }
    }

    private Long entityEnvironnementId(EnvApplicationEntity envApplicationEntity) {
        if ( envApplicationEntity == null ) {
            return null;
        }
        EnvironnementEntity environnement = envApplicationEntity.getEnvironnement();
        if ( environnement == null ) {
            return null;
        }
        Long id = environnement.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityApplicationId(EnvApplicationEntity envApplicationEntity) {
        if ( envApplicationEntity == null ) {
            return null;
        }
        ApplicationEntity application = envApplicationEntity.getApplication();
        if ( application == null ) {
            return null;
        }
        Long id = application.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityApplicationCode(EnvApplicationEntity envApplicationEntity) {
        if ( envApplicationEntity == null ) {
            return null;
        }
        ApplicationEntity application = envApplicationEntity.getApplication();
        if ( application == null ) {
            return null;
        }
        String code = application.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }

    private String entityApplicationLibelle(EnvApplicationEntity envApplicationEntity) {
        if ( envApplicationEntity == null ) {
            return null;
        }
        ApplicationEntity application = envApplicationEntity.getApplication();
        if ( application == null ) {
            return null;
        }
        String libelle = application.getLibelle();
        if ( libelle == null ) {
            return null;
        }
        return libelle;
    }
}
