package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.ApplicationEntity;
import ma.perenity.backend.entities.EnvApplicationEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvApplicationMapper;
import ma.perenity.backend.repository.ApplicationRepository;
import ma.perenity.backend.repository.EnvApplicationRepository;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.service.EnvApplicationService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvApplicationServiceImpl implements EnvApplicationService {

    private final EnvApplicationRepository repository;
    private final EnvironnementRepository environnementRepository;
    private final ApplicationRepository applicationRepository;
    private final EnvApplicationMapper mapper;
    private final PermissionService permissionService;

    @Override
    public List<EnvApplicationDTO> getByEnvironnement(Long envId, String search) {

        EnvironnementEntity env = environnementRepository.findById(envId)
                .orElseThrow(() -> new ResourceNotFoundException("Environnement not found with id = " + envId));

        if (!permissionService.canAccessEnv(env, ActionType.CONSULT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les applications de cet environnement");
        }

        List<EnvApplicationEntity> apps = repository.findByEnvironnementId(envId)
                .stream()
                .filter(EnvApplicationEntity::getActif)
                .toList();

        if (search != null && !search.trim().isEmpty()) {
            String term = search.trim().toLowerCase();

            apps = apps.stream()
                    .filter(a ->
                            (a.getApplication().getCode() != null && a.getApplication().getCode().toLowerCase().contains(term)) ||
                                    (a.getApplication().getLibelle() != null && a.getApplication().getLibelle().toLowerCase().contains(term)) ||
                                    (a.getHost() != null && a.getHost().toLowerCase().contains(term)) ||
                                    (a.getUrl() != null && a.getUrl().toLowerCase().contains(term)) ||
                                    (a.getProtocole() != null && a.getProtocole().toLowerCase().contains(term))
                    )
                    .toList();
        }

        return apps.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public EnvApplicationDTO create(EnvApplicationDTO dto) {

        if (dto.getEnvironnementId() == null) {
            throw new IllegalStateException("EnvironnementId est obligatoire");
        }
        if (dto.getApplicationId() == null) {
            throw new IllegalStateException("ApplicationId est obligatoire");
        }

        EnvironnementEntity env = environnementRepository.findById(dto.getEnvironnementId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Environnement not found with id = " + dto.getEnvironnementId()
                ));

        if (!permissionService.canAccessEnv(env, ActionType.CREATE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit d'ajouter des applications à cet environnement");
        }

        ApplicationEntity app = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id = " + dto.getApplicationId()
                ));

        boolean exists = repository.findByEnvironnementId(dto.getEnvironnementId())
                .stream()
                .filter(EnvApplicationEntity::getActif)
                .anyMatch(a -> a.getApplication().getId().equals(dto.getApplicationId()));

        if (exists) {
            throw new IllegalStateException("Cette application existe déjà dans cet environnement.");
        }

        EnvApplicationEntity entity = mapper.toEntity(dto);
        entity.setId(null);
        entity.setEnvironnement(env);
        entity.setApplication(app);
        entity.setActif(true);

        entity = repository.save(entity);

        return mapper.toDto(entity);
    }

    @Override
    public EnvApplicationDTO update(Long id, EnvApplicationDTO dto) {

        EnvApplicationEntity entity = repository.findById(id)
                .filter(EnvApplicationEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException("EnvApplication not found with id = " + id));

        if (!permissionService.canAccessEnv(entity.getEnvironnement(), ActionType.UPDATE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de modifier cette application d'environnement");
        }

        mapper.updateEntityFromDto(dto, entity);

        entity = repository.save(entity);

        return mapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {

        EnvApplicationEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("EnvApplication not found with id = " + id));

        if (!permissionService.canAccessEnv(entity.getEnvironnement(), ActionType.DELETE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de supprimer cette application d'environnement");
        }

        entity.setActif(false);
        repository.save(entity);
    }

    @Override
    public PaginatedResponse<EnvApplicationDTO> search(PaginationRequest req) {

        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La recherche globale des EnvApplications est réservée à l'administrateur");
        }

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<EnvApplicationEntity> specBuilder = new EntitySpecification<>();

        Page<EnvApplicationEntity> page = repository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
