package ma.perenity.backend.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import ma.perenity.backend.dto.UserCreateUpdateDTO;
import ma.perenity.backend.dto.UserDTO;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.UtilisateurEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T17:27:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDto(UtilisateurEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setProfilId( entityProfilId( entity ) );
        userDTO.setProfilCode( entityProfilCode( entity ) );
        userDTO.setProfilLibelle( entityProfilLibelle( entity ) );
        userDTO.setId( entity.getId() );
        userDTO.setCode( entity.getCode() );
        userDTO.setFirstName( entity.getFirstName() );
        userDTO.setLastName( entity.getLastName() );
        userDTO.setEmail( entity.getEmail() );
        userDTO.setActif( entity.getActif() );

        return userDTO;
    }

    @Override
    public List<UserDTO> toDtoList(List<UtilisateurEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<UserDTO> list = new ArrayList<UserDTO>( entities.size() );
        for ( UtilisateurEntity utilisateurEntity : entities ) {
            list.add( toDto( utilisateurEntity ) );
        }

        return list;
    }

    @Override
    public UtilisateurEntity toEntity(UserCreateUpdateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        UtilisateurEntity.UtilisateurEntityBuilder utilisateurEntity = UtilisateurEntity.builder();

        utilisateurEntity.code( dto.getCode() );
        utilisateurEntity.firstName( dto.getFirstName() );
        utilisateurEntity.lastName( dto.getLastName() );
        utilisateurEntity.email( dto.getEmail() );
        utilisateurEntity.actif( dto.getActif() );

        return utilisateurEntity.build();
    }

    @Override
    public void updateEntityFromDto(UserCreateUpdateDTO dto, UtilisateurEntity entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getCode() != null ) {
            entity.setCode( dto.getCode() );
        }
        if ( dto.getFirstName() != null ) {
            entity.setFirstName( dto.getFirstName() );
        }
        if ( dto.getLastName() != null ) {
            entity.setLastName( dto.getLastName() );
        }
        if ( dto.getEmail() != null ) {
            entity.setEmail( dto.getEmail() );
        }
        if ( dto.getActif() != null ) {
            entity.setActif( dto.getActif() );
        }
    }

    private Long entityProfilId(UtilisateurEntity utilisateurEntity) {
        if ( utilisateurEntity == null ) {
            return null;
        }
        ProfilEntity profil = utilisateurEntity.getProfil();
        if ( profil == null ) {
            return null;
        }
        Long id = profil.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityProfilCode(UtilisateurEntity utilisateurEntity) {
        if ( utilisateurEntity == null ) {
            return null;
        }
        ProfilEntity profil = utilisateurEntity.getProfil();
        if ( profil == null ) {
            return null;
        }
        String code = profil.getCode();
        if ( code == null ) {
            return null;
        }
        return code;
    }

    private String entityProfilLibelle(UtilisateurEntity utilisateurEntity) {
        if ( utilisateurEntity == null ) {
            return null;
        }
        ProfilEntity profil = utilisateurEntity.getProfil();
        if ( profil == null ) {
            return null;
        }
        String libelle = profil.getLibelle();
        if ( libelle == null ) {
            return null;
        }
        return libelle;
    }
}
