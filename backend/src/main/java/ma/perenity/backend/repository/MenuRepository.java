package ma.perenity.backend.repository;

import ma.perenity.backend.entities.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
}
