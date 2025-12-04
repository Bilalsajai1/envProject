// src/main/java/ma/perenity/backend/service/impl/EnvironmentTypeServiceImpl.java
package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvironmentTypeMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.service.EnvironmentTypeService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentTypeServiceImpl implements EnvironmentTypeService {

    private final EnvironmentTypeRepository repository;
    private final EnvironmentTypeMapper mapper;
    private final PermissionService permissionService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administration des types d'environnement réservée à l'administrateur"
            );
        }
    }

    @Override
    public List<EnvironmentTypeDTO> getAll() {
        checkAdmin();
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<EnvironmentTypeDTO> getAllActive() {
        List<EnvironmentTypeEntity> allActives = repository.findByActifTrue();

        if (permissionService.isAdmin()) {
            return allActives.stream()
                    .map(mapper::toDto)
                    .toList();
        }

        return allActives.stream()
                .filter(t -> permissionService.canAccessEnvType(t.getCode(), ActionType.CONSULT))
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public EnvironmentTypeDTO getById(Long id) {
        checkAdmin();
        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with id = " + id
                ));
        return mapper.toDto(entity);
    }

    @Override
    public EnvironmentTypeDTO create(EnvironmentTypeDTO dto) {
        checkAdmin();

        if (repository.existsByCode(dto.getCode())) {
            throw new IllegalStateException("EnvironmentType code already exists: " + dto.getCode());
        }

        EnvironmentTypeEntity entity = mapper.toEntity(dto);
        entity.setActif(true);

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public EnvironmentTypeDTO update(Long id, EnvironmentTypeDTO dto) {
        checkAdmin();

        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with id = " + id
                ));

        if (!entity.getCode().equals(dto.getCode())
                && repository.existsByCode(dto.getCode())) {
            throw new IllegalStateException("EnvironmentType code already exists: " + dto.getCode());
        }

        mapper.updateEntityFromDto(dto, entity);

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        checkAdmin();

        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with id = " + id
                ));

        entity.setActif(false);
        repository.save(entity);
    }

    @Override
    public PaginatedResponse<EnvironmentTypeDTO> search(PaginationRequest req) {
        checkAdmin();

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<EnvironmentTypeEntity> specBuilder = new EntitySpecification<>();

        System.out.println("=== FILTERS DEBUG ===");
        if (req.getFilters() != null) {
            req.getFilters().forEach((k, v) ->
                    System.out.println("Filter: " + k + " = " + v)
            );
        }

        Page<EnvironmentTypeEntity> page = repository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
