package ma.perenity.backend.repository;

import ma.perenity.backend.entities.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<MenuEntity, Long>,
        JpaSpecificationExecutor<MenuEntity> {

    List<MenuEntity> findByEnvironmentType_CodeAndVisibleTrueOrderByOrdreAsc(String code);

}
