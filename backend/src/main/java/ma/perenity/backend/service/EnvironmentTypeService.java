package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvironmentTypeMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentTypeService {

    private final EnvironmentTypeRepository repository;
    private final EnvironmentTypeMapper mapper;

    // ============================================================
    // GET
    // ============================================================

    public List<EnvironmentTypeDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<EnvironmentTypeDTO> getAllActive() {
        return repository.findByActifTrue()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public EnvironmentTypeDTO getById(Long id) {
        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with id = " + id
                ));
        return mapper.toDto(entity);
    }

    // ============================================================
    // CREATE
    // ============================================================

    public EnvironmentTypeDTO create(EnvironmentTypeDTO dto) {

        // Vérifier unicité du code
        if (repository.existsByCode(dto.getCode())) {
            throw new IllegalStateException("EnvironmentType code already exists: " + dto.getCode());
        }

        EnvironmentTypeEntity entity = mapper.toEntity(dto);
        entity.setActif(true);

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public EnvironmentTypeDTO update(Long id, EnvironmentTypeDTO dto) {

        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with id = " + id
                ));

        // Si le code change, on vérifie qu'il n'est pas déjà utilisé
        if (!entity.getCode().equals(dto.getCode())
                && repository.existsByCode(dto.getCode())) {
            throw new IllegalStateException("EnvironmentType code already exists: " + dto.getCode());
        }

        mapper.updateEntityFromDto(dto, entity);

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ============================================================
    // DELETE LOGIQUE
    // ============================================================

    public void delete(Long id) {
        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with id = " + id
                ));

        entity.setActif(false);
        repository.save(entity);
    }

    // ============================================================
    // SEARCH (pagination + filtres dynamiques)
    // ============================================================

    public PaginatedResponse<EnvironmentTypeDTO> search(PaginationRequest req) {

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<EnvironmentTypeEntity> specBuilder = new EntitySpecification<>();

        Page<EnvironmentTypeEntity> page = repository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
