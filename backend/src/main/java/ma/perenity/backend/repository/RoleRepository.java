package ma.perenity.backend.repository;

import ma.perenity.backend.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity>  {

    List<RoleEntity> findByActifTrue();

    List<RoleEntity> findByEnvironnementId(Long environnementId);

    Optional<RoleEntity> findByCode(String code);

}
