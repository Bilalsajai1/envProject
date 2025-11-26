package ma.perenity.backend.repository;

import ma.perenity.backend.entities.UtilisateurEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, Long>, JpaSpecificationExecutor<UtilisateurEntity> {

    Optional<UtilisateurEntity> findByEmail(String email);

}
