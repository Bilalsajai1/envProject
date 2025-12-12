package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.ApplicationEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.ApplicationMapper;
import ma.perenity.backend.repository.ApplicationRepository;
import ma.perenity.backend.service.ApplicationService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.specification.EntitySpecification;
import ma.perenity.backend.utilities.AdminGuard;
import ma.perenity.backend.utilities.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;
    private final PermissionService permissionService;

    @Override
    public List<ApplicationDTO> getAll() {
        AdminGuard.requireAdmin(permissionService, "Administration des applications reservee a l'administrateur");
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<ApplicationDTO> getAllActive() {
        return repository.findByActifTrue()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public ApplicationDTO getById(Long id) {
        AdminGuard.requireAdmin(permissionService, "Administration des applications reservee a l'administrateur");

        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Application not found with id = " + id)
                );

        return mapper.toDto(entity);
    }

    @Override
    public ApplicationDTO create(ApplicationDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des applications reservee a l'administrateur");

        ApplicationEntity entity = mapper.toEntity(dto);

        if (entity.getActif() == null) {
            entity.setActif(Boolean.TRUE);
        }

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public ApplicationDTO update(Long id, ApplicationDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des applications reservee a l'administrateur");

        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Application not found with id = " + id)
                );

        mapper.updateEntityFromDto(dto, entity);

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        AdminGuard.requireAdmin(permissionService, "Administration des applications reservee a l'administrateur");

        ApplicationEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Application not found with id = " + id)
                );

        entity.setActif(false);
        repository.save(entity);
    }

    @Override
    public PaginatedResponse<ApplicationDTO> search(PaginationRequest req) {
        AdminGuard.requireAdmin(permissionService, "Administration des applications reservee a l'administrateur");

        Pageable pageable = PaginationUtils.buildPageable(req);

        Map<String, Object> rawFilters = PaginationUtils.extractFilters(req);
        String search = PaginationUtils.extractSearch(rawFilters);

        EntitySpecification<ApplicationEntity> specBuilder = new EntitySpecification<>();
        Specification<ApplicationEntity> spec = specBuilder.getSpecification(rawFilters);

        if (search != null) {
            final String term = "%" + search.toLowerCase() + "%";

            Specification<ApplicationEntity> globalSearchSpec = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), term),
                    cb.like(cb.lower(root.get("libelle")), term),
                    cb.like(cb.lower(root.get("description")), term)
            );

            spec = spec.and(globalSearchSpec);
        }

        Page<ApplicationEntity> page = repository.findAll(spec, pageable);

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }

}
