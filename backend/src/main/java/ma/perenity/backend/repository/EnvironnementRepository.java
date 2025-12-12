package ma.perenity.backend.repository;

import ma.perenity.backend.entities.EnvironnementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EnvironnementRepository extends
        JpaRepository<EnvironnementEntity, Long>,
        JpaSpecificationExecutor<EnvironnementEntity> {

    List<EnvironnementEntity> findByProjet_IdAndType_CodeAndActifTrue(Long projetId, String typeCode);
}
