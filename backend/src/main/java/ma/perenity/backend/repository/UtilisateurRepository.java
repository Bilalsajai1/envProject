package ma.perenity.backend.repository;

import ma.perenity.backend.entities.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends
        JpaRepository<UtilisateurEntity, Long>,
        JpaSpecificationExecutor<UtilisateurEntity> {

    Optional<UtilisateurEntity> findByEmail(String email);

    Optional<UtilisateurEntity> findByCode(String code);

    @Query("""
                SELECT u
                FROM UtilisateurEntity u
                JOIN FETCH u.profil
                WHERE u.email = :email
            """)
    Optional<UtilisateurEntity> findByEmailWithProfil(@Param("email") String email);


    List<UtilisateurEntity> findByActifTrueAndIsDeletedFalse();

    long countByProfil_IdAndActifTrueAndIsDeletedFalse(Long profilId);
}
