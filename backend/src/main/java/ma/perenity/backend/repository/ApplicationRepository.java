package ma.perenity.backend.repository;

import ma.perenity.backend.entities.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ApplicationRepository extends
        JpaRepository<ApplicationEntity, Long>,
        JpaSpecificationExecutor<ApplicationEntity> {

    List<ApplicationEntity> findByActifTrue();
}
