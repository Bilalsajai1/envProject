package ma.perenity.backend.repository;

import ma.perenity.backend.entities.EnvApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvApplicationRepository extends JpaRepository<EnvApplicationEntity, Long> {

    List<EnvApplicationEntity> findByEnvironnementId(Long environnementId);

    long countByActifTrue();

}
