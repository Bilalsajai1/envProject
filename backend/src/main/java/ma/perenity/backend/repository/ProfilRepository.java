package ma.perenity.backend.repository;


import ma.perenity.backend.entities.ProfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfilRepository extends JpaRepository<ProfilEntity, Long> ,
        JpaSpecificationExecutor<ProfilEntity> {
}

