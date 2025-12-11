package ma.perenity.backend.service.impl;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.ProjetMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.ProjetService;
import ma.perenity.backend.service.util.AdminGuard;
import ma.perenity.backend.service.util.PaginationUtils;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetServiceImpl implements ProjetService {

    private final ProjetRepository projetRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final ProjetMapper projetMapper;
    private final PermissionService permissionService;

    @Override
    public List<ProjetDTO> getProjectsByEnvironmentType(String typeCode, String search) {
        boolean canConsultType = permissionService.canViewEnvironmentType(typeCode);
        boolean canCreateType = permissionService.canAccessEnvType(typeCode, ActionType.CREATE);

        if (!canConsultType && !canCreateType) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les projets pour le type " + typeCode
            );
        }

        List<ProjetEntity> projets = projetRepository.findByEnvironmentTypeCode(typeCode);

        if (!permissionService.isAdmin()) {
            final boolean allowByCreateOnType = canCreateType;
            projets = projets.stream()
                    .filter(p -> allowByCreateOnType || permissionService.canConsultProject(p.getId()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            final String term = search.trim().toLowerCase();
            projets = projets.stream()
                    .filter(p ->
                            (p.getCode() != null && p.getCode().toLowerCase().contains(term)) ||
                                    (p.getLibelle() != null && p.getLibelle().toLowerCase().contains(term)) ||
                                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(term))
                    )
                    .collect(Collectors.toList());
        }

        return projets.stream()
                .map(projetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjetDTO> getAll() {
        AdminGuard.requireAdmin(permissionService, "La consultation de tous les projets est reservee a l'administrateur");

        return projetRepository.findByActifTrue()
                .stream()
                .map(projetMapper::toDto)
                .toList();
    }

    @Override
    public ProjetDTO getById(Long id) {
        if (!permissionService.isAdmin() &&
                !permissionService.canAccessProjectById(id, ActionType.CONSULT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter ce projet");
        }

        ProjetEntity entity = projetRepository.findById(id)
                .filter(ProjetEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet introuvable (ou inactif) avec id = " + id
                ));

        return projetMapper.toDto(entity);
    }

    @Override
    public ProjetDTO create(ProjetDTO dto) {
        if (!permissionService.isAdmin()) {
            List<String> codes = resolveEnvTypeCodes(dto);
            if (codes.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Type d'environnement requis pour la creation de projet");
            }
            for (String code : codes) {
                if (!permissionService.canAccessEnvType(code, ActionType.CREATE)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "La creation de projet n'est pas autorisee pour le type " + code);
                }
            }
        }

        ProjetEntity entity = projetMapper.toEntity(dto);
        entity.setId(null);
        applyEnvTypes(dto, entity);

        if (entity.getActif() == null) {
            entity.setActif(true);
        }

        entity = projetRepository.save(entity);
        return projetMapper.toDto(entity);
    }

    @Override
    public ProjetDTO update(Long id, ProjetDTO dto) {
        ProjetEntity entity = projetRepository.findById(id)
                .filter(ProjetEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet introuvable (ou inactif) avec id = " + id
                ));

        boolean allowed = permissionService.isAdmin() || permissionService.canAccessProject(entity, ActionType.UPDATE);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de modifier ce projet");
        }

        projetMapper.updateEntityFromDto(dto, entity);
        applyEnvTypes(dto, entity);

        entity = projetRepository.save(entity);
        return projetMapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        ProjetEntity projet = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec id = " + id));

        boolean allowed = permissionService.isAdmin() || permissionService.canAccessProject(projet, ActionType.DELETE);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de supprimer ce projet");
        }

        projet.setActif(false);
        projetRepository.save(projet);
    }

    @Override
    public PaginatedResponse<ProjetDTO> search(PaginationRequest req) {
        Pageable pageable = PaginationUtils.buildPageable(req);

        Map<String, Object> rawFilters = PaginationUtils.extractFilters(req);
        String search = PaginationUtils.extractSearch(rawFilters);

        String typeCode = null;
        Object typeObj = rawFilters.remove("typeCode");
        if (typeObj != null) {
            typeCode = typeObj.toString().trim().toUpperCase();
        }

        boolean canCreateType = typeCode != null && permissionService.canAccessEnvType(typeCode, ActionType.CREATE);
        if (typeCode != null && !permissionService.canViewEnvironmentType(typeCode) && !canCreateType) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les projets pour le type " + typeCode
            );
        }

        EntitySpecification<ProjetEntity> specBuilder = new EntitySpecification<>();
        Specification<ProjetEntity> spec = specBuilder.getSpecification(rawFilters);

        spec = spec.and((root, query, cb) ->
                cb.isTrue(root.get("actif"))
        );

        if (typeCode != null) {
            final String finalType = typeCode;
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                var envJoin = root.join("environnements", JoinType.LEFT);
                var typeJoin = envJoin.join("type", JoinType.LEFT);
                var typeJoin2 = root.join("environmentTypes", JoinType.LEFT);
                return cb.or(
                        cb.equal(typeJoin.get("code"), finalType),
                        cb.equal(typeJoin2.get("code"), finalType)
                );
            });
        }

        if (search != null) {
            final String term = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("code")), term),
                            cb.like(cb.lower(root.get("libelle")), term),
                            cb.like(cb.lower(root.get("description")), term)
                    )
            );
        }

        Page<ProjetEntity> page = projetRepository.findAll(spec, pageable);

        if (!permissionService.isAdmin()) {
            final boolean allowByCreateOnType = canCreateType;
            List<ProjetEntity> filteredProjects = page.getContent().stream()
                    .filter(p -> allowByCreateOnType || permissionService.canConsultProject(p.getId()))
                    .collect(Collectors.toList());

            page = new PageImpl<>(
                    filteredProjects,
                    pageable,
                    filteredProjects.size()
            );
        }

        return PaginatedResponse.fromPage(
                page.map(projetMapper::toDto)
        );
    }

    private List<String> resolveEnvTypeCodes(ProjetDTO dto) {
        if (dto.getEnvTypeCodes() != null && !dto.getEnvTypeCodes().isEmpty()) {
            return dto.getEnvTypeCodes();
        }
        if (dto.getEnvTypeCode() != null && !dto.getEnvTypeCode().isBlank()) {
            return Collections.singletonList(dto.getEnvTypeCode());
        }
        return Collections.emptyList();
    }

    private void applyEnvTypes(ProjetDTO dto, ProjetEntity entity) {
        List<String> codes = resolveEnvTypeCodes(dto);
        if (codes.isEmpty()) {
            entity.getEnvironmentTypes().clear();
            return;
        }
        List<EnvironmentTypeEntity> types = environmentTypeRepository.findByCodeIn(codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .map(String::toUpperCase)
                .toList());
        entity.getEnvironmentTypes().clear();
        entity.getEnvironmentTypes().addAll(types);
    }
}
