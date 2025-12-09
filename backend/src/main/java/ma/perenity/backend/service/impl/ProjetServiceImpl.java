// src/main/java/ma/perenity/backend/service/impl/ProjetServiceImpl.java
package ma.perenity.backend.service.impl;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.ProjetMapper;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.ProjetService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetServiceImpl implements ProjetService {

    private final ProjetRepository projetRepository;
    private final ProjetMapper projetMapper;
    private final PermissionService permissionService;

    @Override
    public List<ProjetDTO> getProjectsByEnvironmentType(String typeCode, String search) {
        if (!permissionService.canViewEnvironmentType(typeCode)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les projets pour le type " + typeCode
            );
        }

        List<ProjetEntity> projets = projetRepository.findByEnvironmentTypeCode(typeCode);

        if (!permissionService.isAdmin()) {
            projets = projets.stream()
                    .filter(p -> permissionService.canConsultProject(p.getId()))
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

        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La consultation de tous les projets est réservée à l'administrateur");
        }

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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La création de projet est réservée à l'administrateur");
        }

        ProjetEntity entity = projetMapper.toEntity(dto);
        entity.setId(null);

        if (entity.getActif() == null) {
            entity.setActif(true);
        }

        entity = projetRepository.save(entity);
        return projetMapper.toDto(entity);
    }

    @Override
    public ProjetDTO update(Long id, ProjetDTO dto) {

        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La mise à jour de projet est réservée à l'administrateur");
        }

        ProjetEntity entity = projetRepository.findById(id)
                .filter(ProjetEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet introuvable (ou inactif) avec id = " + id
                ));

        projetMapper.updateEntityFromDto(dto, entity);

        entity = projetRepository.save(entity);
        return projetMapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {

        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La suppression de projet est réservée à l'administrateur");
        }

        ProjetEntity projet = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec id = " + id));

        projet.setActif(false);

        projetRepository.save(projet);
    }

    @Override
    public PaginatedResponse<ProjetDTO> search(PaginationRequest req) {

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Map<String, Object> rawFilters = req.getFilters() != null
                ? new HashMap<>(req.getFilters())
                : new HashMap<>();

        // Extraire search
        String search = null;
        Object searchObj = rawFilters.remove("search");
        if (searchObj != null) {
            search = searchObj.toString().trim();
            if (search.isEmpty()) search = null;
        }

        // Extraire typeCode
        String typeCode = null;
        Object typeObj = rawFilters.remove("typeCode");
        if (typeObj != null) {
            typeCode = typeObj.toString().trim().toUpperCase();
        }

        // 🔐 Vérifier les permissions pour le type d'environnement
        if (typeCode != null && !permissionService.canViewEnvironmentType(typeCode)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les projets pour le type " + typeCode
            );
        }

        EntitySpecification<ProjetEntity> specBuilder = new EntitySpecification<>();
        Specification<ProjetEntity> spec = specBuilder.getSpecification(rawFilters);

        // Filtre actif = true
        spec = spec.and((root, query, cb) ->
                cb.isTrue(root.get("actif"))
        );

        // 🔥 FILTRE PAR TYPE D'ENVIRONNEMENT
        if (typeCode != null) {
            final String finalType = typeCode;
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                var envJoin = root.join("environnements", JoinType.LEFT);
                var typeJoin = envJoin.join("type", JoinType.LEFT);
                return cb.equal(typeJoin.get("code"), finalType);
            });
        }

        // 🔎 RECHERCHE GLOBALE
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

        // 🔐 FILTRAGE PAR PERMISSIONS (pour les non-admins)
        if (!permissionService.isAdmin()) {
            List<ProjetEntity> filteredProjects = page.getContent().stream()
                    .filter(p -> permissionService.canConsultProject(p.getId()))
                    .collect(Collectors.toList());

            // Recréer une Page avec les projets filtrés
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

}
