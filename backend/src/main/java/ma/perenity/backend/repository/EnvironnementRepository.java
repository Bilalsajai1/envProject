package ma.perenity.backend.repository;

import ma.perenity.backend.entities.EnvironnementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnvironnementRepository extends
        JpaRepository<EnvironnementEntity, Long>,
        JpaSpecificationExecutor<EnvironnementEntity> {

    @Query("""
           SELECT e
           FROM EnvironnementEntity e
           JOIN e.projet p
           JOIN e.type t
           WHERE p.id = :projetId
             AND t.code = :typeCode
           ORDER BY e.code
           """)
    List<EnvironnementEntity> findByProjetAndType(
            @Param("projetId") Long projetId,
            @Param("typeCode") String typeCode
    );

    long countByActifTrue();


    List<EnvironnementEntity> findByProjet_IdAndType_CodeAndActifTrue(Long projetId, String typeCode);
}
