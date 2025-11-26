package ma.perenity.backend.repository;

import ma.perenity.backend.entities.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> ,
        JpaSpecificationExecutor<MenuEntity> {
    @Query("""
                SELECT DISTINCT m
                FROM MenuEntity m
                JOIN m.roles r
                WHERE r.id IN :roleIds
                ORDER BY m.ordre ASC
            """)
    List<MenuEntity> findMenusByRoles(@Param("roleIds") List<Long> roleIds);

    List<MenuEntity> findByEnvironmentType_CodeAndVisibleTrueOrderByOrdreAsc(String code);


}
