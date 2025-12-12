package ma.perenity.backend.repository;

import ma.perenity.backend.entities.ProfilRoleEntity;
import ma.perenity.backend.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfilRoleRepository extends JpaRepository<ProfilRoleEntity, Long> {

    @Query("""
                SELECT pr.role
                FROM ProfilRoleEntity pr
                LEFT JOIN FETCH pr.role.projet
                WHERE pr.profil.id = :profilId
            """)
    List<RoleEntity> findRolesByProfil(@Param("profilId") Long profilId);

    @Query("""
        SELECT pr.role.id
        FROM ProfilRoleEntity pr
        WHERE pr.profil.id = :profilId
    """)
    List<Long> findRoleIdsByProfilId(@Param("profilId") Long profilId);

    @Modifying
    @Query("DELETE FROM ProfilRoleEntity pr WHERE pr.profil.id = :profilId")
    void deleteByProfilId(@Param("profilId") Long profilId);
    @Modifying
    @Query("""
                DELETE FROM ProfilRoleEntity pr
                WHERE pr.profil.id = :profilId
                  AND pr.role.code LIKE CONCAT(:prefix, '%')
            """)
    void deleteByProfilIdAndRoleCodePrefix(@Param("profilId") Long profilId,
                                           @Param("prefix") String prefix);

}
