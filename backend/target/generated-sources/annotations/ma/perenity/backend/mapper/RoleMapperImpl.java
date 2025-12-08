package ma.perenity.backend.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.RoleDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.RoleEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleDTO toDto(RoleEntity role) {
        if ( role == null ) {
            return null;
        }

        RoleDTO.RoleDTOBuilder roleDTO = RoleDTO.builder();

        roleDTO.environnementId( roleEnvironnementId( role ) );
        roleDTO.environnementCode( roleEnvironnementCode( role ) );
        roleDTO.projetId( roleProjetId( role ) );
        roleDTO.projetCode( roleProjetCode( role ) );
        roleDTO.id( role.getId() );
        roleDTO.code( role.getCode() );
        roleDTO.libelle( role.getLibelle() );
        roleDTO.actif( role.getActif() );

        roleDTO.action( role.getAction() != null ? role.getAction().name() : null );

        return roleDTO.build();
    }

    @Override
    public List<RoleDTO> toDtoList(List<RoleEntity> roles) {
        if ( roles == null ) {
            return null;
        }

        List<RoleDTO> list = new ArrayList<RoleDTO>( roles.size() );
        for ( RoleEntity roleEntity : roles ) {
            list.add( toDto( roleEntity ) );
        }

        return list;
    }

    private Long roleEnvironnementId(RoleEntity roleEntity) {
        if ( roleEntity == null ) {
            return null;
        }
        EnvironnementEntity environnement = roleEntity.getEnvironnement();
        if ( environnement == null ) {
            return null;
        }
        Long id = environnement.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String roleEnvironnementCode(RoleEntity roleEntity) {
        if ( roleEntity == null ) {
            return null;
        }
        EnvironnementEntity environnement = roleEntity.getEnvironnement();
        if ( environnement == null ) {
            return null;
        }
        String code = environnement.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }

    private Long roleProjetId(RoleEntity roleEntity) {
        if ( roleEntity == null ) {
            return null;
        }
        ProjetEntity projet = roleEntity.getProjet();
        if ( projet == null ) {
            return null;
        }
        Long id = projet.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String roleProjetCode(RoleEntity roleEntity) {
        if ( roleEntity == null ) {
            return null;
        }
        ProjetEntity projet = roleEntity.getProjet();
        if ( projet == null ) {
            return null;
        }
        String code = projet.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }
}
