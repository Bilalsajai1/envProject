package ma.perenity.backend.repository;

import ma.perenity.backend.dto.ProfilActiveUserCountView;
import ma.perenity.backend.entities.ProfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfilRepository extends JpaRepository<ProfilEntity, Long>,
        JpaSpecificationExecutor<ProfilEntity> {
    List<ProfilEntity> findByActifTrueAndIsDeletedFalse();

    boolean existsByCode(String code);

    @Query("""
                SELECT p.id AS profilId, COUNT(u.id) AS activeUserCount
                FROM ProfilEntity p
                LEFT JOIN p.utilisateurs u ON u.actif = true AND u.isDeleted = false
                WHERE p.id IN :profilIds
                GROUP BY p.id
            """)
    List<ProfilActiveUserCountView> countActiveUsersByProfilIds(@Param("profilIds") List<Long> profilIds);

}
