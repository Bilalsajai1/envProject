package ma.perenity.backend.repository;

import ma.perenity.backend.entities.ProjetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjetRepository extends
        JpaRepository<ProjetEntity, Long>,
        JpaSpecificationExecutor<ProjetEntity> {

    @Query("""
            SELECT DISTINCT p
            FROM ProjetEntity p
            JOIN p.environnements e
            JOIN e.type t
            WHERE t.code = :typeCode
            """)
    List<ProjetEntity> findByEnvironmentTypeCode(@Param("typeCode") String typeCode);

    List<ProjetEntity> findByActifTrue();

    boolean existsByCode(String code);
}
