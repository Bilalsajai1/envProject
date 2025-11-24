package ma.perenity.backend.repository;

import ma.perenity.backend.entities.ProfilRoleEntity;
import ma.perenity.backend.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfilRoleRepository extends JpaRepository<ProfilRoleEntity, Long> {

    @Query("""
                SELECT pr.role
                FROM ProfilRoleEntity pr
                WHERE pr.profil.id = :profilId
            """)
    List<RoleEntity> findRolesByProfil(@Param("profilId") Long profilId);
}