package ma.perenity.backend.repository;

import ma.perenity.backend.entities.ProfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProfilRepository extends JpaRepository<ProfilEntity, Long>,
        JpaSpecificationExecutor<ProfilEntity> {
    List<ProfilEntity> findByActifTrueAndIsDeletedFalse();


    boolean existsByCode(String code);
}
