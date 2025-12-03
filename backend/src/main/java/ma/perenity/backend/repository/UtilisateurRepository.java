package ma.perenity.backend.repository;

import ma.perenity.backend.entities.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends
        JpaRepository<UtilisateurEntity, Long>,
        JpaSpecificationExecutor<UtilisateurEntity> {

    Optional<UtilisateurEntity> findByEmail(String email);
    List<UtilisateurEntity> findByActifTrue();

}
