package ma.perenity.backend.repository;

import ma.perenity.backend.entities.EnvironmentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EnvironmentTypeRepository extends
        JpaRepository<EnvironmentTypeEntity, Long>,
        JpaSpecificationExecutor<EnvironmentTypeEntity> {

    List<EnvironmentTypeEntity> findByActifTrue();

    Optional<EnvironmentTypeEntity> findByCode(String code);

    boolean existsByCode(String code);
}
