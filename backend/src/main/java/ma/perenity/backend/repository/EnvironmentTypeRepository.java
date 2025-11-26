package ma.perenity.backend.repository;

import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvironmentTypeRepository extends JpaRepository<EnvironmentTypeEntity, Long> {
    List<EnvironmentTypeEntity> findByActifTrue();
    Optional<EnvironmentTypeEntity> findByCode(String code);
}