package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.ApplicationEntity;
import ma.perenity.backend.entities.EnvApplicationEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvApplicationMapper;
import ma.perenity.backend.repository.ApplicationRepository;
import ma.perenity.backend.repository.EnvApplicationRepository;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvApplicationService {

    private final EnvApplicationRepository repository;
    private final EnvironnementRepository environnementRepository;
    private final ApplicationRepository applicationRepository;

    private final EnvApplicationMapper mapper;

    // ============================================================
    // GET BY ENV (ACTIFS UNIQUEMENT)
    // ============================================================
    public List<EnvApplicationDTO> getByEnvironnement(Long envId) {

        environnementRepository.findById(envId)
                .orElseThrow(() -> new ResourceNotFoundException("Environnement not found"));

        return repository.findByEnvironnementId(envId)
                .stream()
                .filter(EnvApplicationEntity::getActif) // logique : n'afficher que les actifs
                .map(mapper::toDto)
                .toList();
    }


    // ============================================================
    // CREATE
    // ============================================================
    public EnvApplicationDTO create(EnvApplicationDTO dto) {

        EnvironnementEntity env = environnementRepository.findById(dto.getEnvironnementId())
                .orElseThrow(() -> new ResourceNotFoundException("Environnement not found"));

        ApplicationEntity app = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // Vérification doublon
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
        entity.setCreatedAt(LocalDateTime.now());

        entity = repository.save(entity);

        return mapper.toDto(entity);
    }


    // ============================================================
    // UPDATE
    // ============================================================
    public EnvApplicationDTO update(Long id, EnvApplicationDTO dto) {

        EnvApplicationEntity entity = repository.findById(id)
                .filter(EnvApplicationEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException("EnvApplication not found"));

        mapper.updateEntity(entity, dto);

        entity.setUpdatedAt(LocalDateTime.now());

        entity = repository.save(entity);

        return mapper.toDto(entity);
    }


    // ============================================================
    // DELETE LOGIQUE
    // ============================================================
    public void delete(Long id) {

        EnvApplicationEntity entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("EnvApplication not found with id = " + id));

        entity.setActif(false);
        entity.setUpdatedAt(LocalDateTime.now());

        repository.save(entity);
    }
    public PaginatedResponse<EnvApplicationDTO> search(PaginationRequest req) {

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<EnvApplicationEntity> specBuilder = new EntitySpecification<>();

        Page<EnvApplicationEntity> page = repository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        // On peut filtrer actif ici si tu veux:
        Page<EnvApplicationEntity> filtered = page.map(e -> e)
                .map(e -> e) // no-op juste pour illustrer, ou filtrer avant
                ;

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
