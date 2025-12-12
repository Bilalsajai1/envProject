package ma.perenity.backend.repository;

import ma.perenity.backend.entities.ProjetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProjetRepository extends
        JpaRepository<ProjetEntity, Long>,
        JpaSpecificationExecutor<ProjetEntity> {

    @Query("""
            SELECT DISTINCT p
            FROM ProjetEntity p
            LEFT JOIN p.environnements e
            LEFT JOIN e.type t
            LEFT JOIN p.environmentTypes et
            WHERE t.code = :typeCode OR et.code = :typeCode
            """)
    List<ProjetEntity> findByEnvironmentTypeCode(@Param("typeCode") String typeCode);

    List<ProjetEntity> findByActifTrue();

    @Query("""
            SELECT DISTINCT p
            FROM ProjetEntity p
            LEFT JOIN FETCH p.environnements e
            LEFT JOIN FETCH e.type
            LEFT JOIN FETCH p.environmentTypes
            WHERE p.actif = true
            """)
    List<ProjetEntity> findByActifTrueWithEnvironments();

    List<ProjetEntity> findByIdInAndActifTrue(Set<Long> ids);

}
