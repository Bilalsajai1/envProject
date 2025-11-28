package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.ApplicationEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.ApplicationMapper;
import ma.perenity.backend.repository.ApplicationRepository;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;
    private final PermissionService permissionService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administration des applications réservée à l'administrateur");
        }
    }

    // ============================================================
    // GET ALL (actifs + inactifs) - ADMIN
    // ============================================================
    public List<ApplicationDTO> getAll() {
        checkAdmin();
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================================================
    // GET ALL ACTIVES UNIQUEMENT - accessible à tout le monde
    // (utilisé par les écrans qui doivent lister les applications)
    // ============================================================
    public List<ApplicationDTO> getAllActive() {
        return repository.findByActifTrue()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================================================
    // GET BY ID (only active) - ADMIN
    // ============================================================
    public ApplicationDTO getById(Long id) {
        checkAdmin();

        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif) // ne renvoyer que les actifs
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        return mapper.toDto(entity);
    }

    // ============================================================
    // CREATE - ADMIN
    // ============================================================
    public ApplicationDTO create(ApplicationDTO dto) {
        checkAdmin();

        ApplicationEntity entity = mapper.toEntity(dto);

        if (entity.getActif() == null) {
            entity.setActif(Boolean.TRUE);
        }

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ============================================================
    // UPDATE (seulement si actif) - ADMIN
    // ============================================================
    public ApplicationDTO update(Long id, ApplicationDTO dto) {
        checkAdmin();

        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        mapper.updateEntityFromDto(dto, entity);

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ============================================================
    // DELETE LOGIQUE - ADMIN
    // ============================================================
    public void delete(Long id) {
        checkAdmin();

        ApplicationEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        entity.setActif(false);
        repository.save(entity);
    }

    // ============================================================
    // SEARCH (avec filtres dynamiques + pagination) - ADMIN
    // ============================================================
    public PaginatedResponse<ApplicationDTO> search(PaginationRequest req) {
        checkAdmin();

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<ApplicationEntity> specBuilder = new EntitySpecification<>();

        Page<ApplicationEntity> page = repository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
