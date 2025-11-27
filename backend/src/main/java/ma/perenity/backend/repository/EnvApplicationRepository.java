package ma.perenity.backend.repository;

import ma.perenity.backend.entities.EnvApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EnvApplicationRepository extends
        JpaRepository<EnvApplicationEntity, Long>,
        JpaSpecificationExecutor<EnvApplicationEntity> {

    List<EnvApplicationEntity> findByEnvironnementId(Long environnementId);

    long countByActifTrue();

    boolean existsByEnvironnementIdAndApplicationIdAndActifTrue(Long environnementId, Long applicationId);

    List<EnvApplicationEntity> findByEnvironnementIdAndActifTrue(Long environnementId);
}
