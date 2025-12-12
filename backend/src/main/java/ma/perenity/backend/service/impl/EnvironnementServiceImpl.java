package ma.perenity.backend.service.impl;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvironnementMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.service.EnvironnementService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.specification.EntitySpecification;
import ma.perenity.backend.utilities.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ma.perenity.backend.excepion.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class EnvironnementServiceImpl implements EnvironnementService {

    private final EnvironnementRepository environnementRepository;
    private final ProjetRepository projetRepository;
    private final EnvironmentTypeRepository typeRepository;
    private final EnvironnementMapper mapper;
    private final PermissionService permissionService;

    @Override
    public List<EnvironnementDTO> getEnvironmentsByProjetAndType(Long projetId, String typeCode, String search) {

        if (!permissionService.canAccessEnvType(typeCode, ActionType.CONSULT)) {
            throw new ForbiddenException(ErrorMessage.NO_PERMISSION);
        }

        ProjetEntity projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet not found"));

        if (!permissionService.canAccessProject(projet, ActionType.CONSULT)) {
            throw new ForbiddenException(ErrorMessage.NO_PERMISSION);
        }

        List<EnvironnementEntity> list =
                environnementRepository.findByProjet_IdAndType_CodeAndActifTrue(projetId, typeCode);

        if (search != null && !search.trim().isEmpty()) {
            String term = search.trim().toLowerCase();
            list = list.stream().filter(e ->
                    (e.getCode() != null && e.getCode().toLowerCase().contains(term)) ||
                            (e.getLibelle() != null && e.getLibelle().toLowerCase().contains(term)) ||
                            (e.getDescription() != null && e.getDescription().toLowerCase().contains(term))
            ).toList();
        }

        return list.stream().map(mapper::toDto).toList();
    }


    @Override
    public EnvironnementEntity getByIdOrThrow(Long id) {
        return environnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Environnement not found with id = " + id
                ));
    }

    @Override
    public EnvironnementDTO create(EnvironnementDTO dto) {

        ProjetEntity projet = projetRepository.findById(dto.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet not found with id = " + dto.getProjetId()
                ));

        EnvironmentTypeEntity type = typeRepository.findByCode(dto.getTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with code = " + dto.getTypeCode()
                ));

        if (!permissionService.canAccessEnvType(type.getCode(), ActionType.CREATE)
                || !permissionService.canAccessProject(projet, ActionType.CREATE)) {
            throw new ForbiddenException(ErrorMessage.NO_CREATE_PERMISSION);
        }

        EnvironnementEntity env = mapper.toEntity(dto);

        env.setId(null);
        env.setProjet(projet);
        env.setType(type);
        env.setActif(true);

        env = environnementRepository.save(env);

        return mapper.toDto(env);
    }

    @Override
    public EnvironnementDTO update(Long id, EnvironnementDTO dto) {

        EnvironnementEntity env = getByIdOrThrow(id);

        if (!permissionService.canAccessEnv(env, ActionType.UPDATE)) {
            throw new ForbiddenException(ErrorMessage.NO_UPDATE_PERMISSION_FOR_RECORD);
        }

        mapper.updateEntityFromDto(dto, env);

        env = environnementRepository.save(env);

        return mapper.toDto(env);
    }

    @Override
    public void delete(Long id) {
        EnvironnementEntity entity = getByIdOrThrow(id);

        if (!permissionService.canAccessEnv(entity, ActionType.DELETE)) {
            throw new ForbiddenException(ErrorMessage.NO_DELETE_PERMISSION_FOR_RECORD);
        }

        entity.setActif(false);

        environnementRepository.save(entity);
    }

    @Override
    public PaginatedResponse<EnvironnementDTO> search(PaginationRequest req) {

        Pageable pageable = PaginationUtils.buildPageable(req);

        Map<String, Object> rawFilters = PaginationUtils.extractFilters(req);

        String search = PaginationUtils.extractSearch(rawFilters);

        Long projetId = null;
        if (rawFilters.containsKey("projetId")) {
            projetId = Long.valueOf(rawFilters.remove("projetId").toString());
        }

        String typeCode = null;
        if (rawFilters.containsKey("typeCode")) {
            typeCode = rawFilters.remove("typeCode").toString().trim().toUpperCase();
        }

        boolean isAdmin = permissionService.isAdmin();

        if (!isAdmin) {
            if (projetId == null || typeCode == null) {
                throw new ForbiddenException(ErrorMessage.NO_READ_PERMISSION);
            }
            if (!permissionService.canAccessEnvType(typeCode, ActionType.CONSULT)
                    || !permissionService.canAccessProjectById(projetId, ActionType.CONSULT)) {
                throw new ForbiddenException(ErrorMessage.NO_READ_PERMISSION);
            }
            rawFilters.clear();
        }

        EntitySpecification<EnvironnementEntity> specBuilder = new EntitySpecification<>();
        Specification<EnvironnementEntity> spec = specBuilder.getSpecification(rawFilters);

        spec = spec.and((root, query, cb) ->
                cb.isTrue(root.get("actif"))
        );

        if (projetId != null) {
            final Long pid = projetId;
            spec = spec.and((root, query, cb) -> {
                var projetJoin = root.join("projet", JoinType.LEFT);
                return cb.equal(projetJoin.get("id"), pid);
            });
        }

        if (typeCode != null) {
            final String tcode = typeCode;
            spec = spec.and((root, query, cb) -> {
                var typeJoin = root.join("type", JoinType.LEFT);
                return cb.equal(typeJoin.get("code"), tcode);
            });
        }

        if (search != null) {
            final String term = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                var projetJoin = root.join("projet", JoinType.LEFT);
                var typeJoin = root.join("type", JoinType.LEFT);

                return cb.or(
                        cb.like(cb.lower(root.get("code")), term),
                        cb.like(cb.lower(root.get("libelle")), term),
                        cb.like(cb.lower(root.get("description")), term),
                        cb.like(cb.lower(projetJoin.get("code")), term),
                        cb.like(cb.lower(projetJoin.get("libelle")), term),
                        cb.like(cb.lower(typeJoin.get("code")), term),
                        cb.like(cb.lower(typeJoin.get("libelle")), term)
                );
            });
        }

        Page<EnvironnementEntity> page = environnementRepository.findAll(spec, pageable);

        return PaginatedResponse.fromPage(page.map(mapper::toDto));
    }
}
