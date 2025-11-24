package ma.perenity.backend.repository;

import ma.perenity.backend.entities.EnvironmentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnvironmentTypeRepository extends JpaRepository<EnvironmentTypeEntity, Long> {
    List<EnvironmentTypeEntity> findByActifTrue();
}