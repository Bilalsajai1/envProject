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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;

    // ============================================================
    // GET ALL (actifs + inactifs)
    // ============================================================
    public List<ApplicationDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================================================
    // GET ALL ACTIVES UNIQUEMENT
    // ============================================================
    public List<ApplicationDTO> getAllActive() {
        return repository.findByActifTrue()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================================================
    // GET BY ID (only active)
    // ============================================================
    public ApplicationDTO getById(Long id) {
        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif) // ne renvoyer que les actifs
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        return mapper.toDto(entity);
    }

    // ============================================================
    // CREATE
    // ============================================================
    public ApplicationDTO create(ApplicationDTO dto) {
        ApplicationEntity entity = mapper.toEntity(dto);

        // si non renseigné côté front, on force à true
        if (entity.getActif() == null) {
            entity.setActif(Boolean.TRUE);
        }

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ============================================================
    // UPDATE (seulement si actif)
    // ============================================================
    public ApplicationDTO update(Long id, ApplicationDTO dto) {

        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        // mappage partiel : uniquement les champs non nulls du DTO
        mapper.updateEntityFromDto(dto, entity);

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ============================================================
    // DELETE LOGIQUE
    // ============================================================
    public void delete(Long id) {

        ApplicationEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        entity.setActif(false);
        repository.save(entity);
    }

    // ============================================================
    // SEARCH (avec filtres dynamiques + pagination)
    // ============================================================
    public PaginatedResponse<ApplicationDTO> search(PaginationRequest req) {

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
