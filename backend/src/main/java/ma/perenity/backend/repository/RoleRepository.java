package ma.perenity.backend.repository;

import ma.perenity.backend.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity>  {

    List<RoleEntity> findByActifTrue();

    List<RoleEntity> findByMenuId(Long menuId);

    List<RoleEntity> findByEnvironnementId(Long environnementId);

    Optional<RoleEntity> findByCode(String code);

    @Query("SELECT r FROM RoleEntity r WHERE r.menu.id = :menuId")
    List<RoleEntity> findRolesByMenu(@Param("menuId") Long menuId);

    @Query("SELECT r FROM RoleEntity r WHERE r.menu IS NULL")
    List<RoleEntity> findRolesWithoutMenu();

}
