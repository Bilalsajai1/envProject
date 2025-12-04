// src/main/java/ma/perenity/backend/service/impl/ProjetServiceImpl.java
package ma.perenity.backend.service.impl;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetServiceImpl implements ProjetService {

    private final ProjetRepository projetRepository;
    private final ProjetMapper projetMapper;
    private final PermissionService permissionService;

    @Override
    public List<ProjetDTO> getProjectsByEnvironmentType(String typeCode) {

        if (!permissionService.canAccessEnvType(typeCode, ActionType.CONSULT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les projets pour le type " + typeCode);
        }

        List<ProjetEntity> projets = projetRepository.findByEnvironmentTypeCode(typeCode);

        if (permissionService.isAdmin()) {
            return projets.stream()
                    .map(projetMapper::toDto)
                    .toList();
        }

        return projets.stream()
                .filter(p -> permissionService.canAccessProject(p, ActionType.CONSULT))
                .map(projetMapper::toDto)
                .toList();
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

        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La recherche globale des projets est réservée à l'administrateur");
        }

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<ProjetEntity> specBuilder = new EntitySpecification<>();

        Page<ProjetEntity> page = projetRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(projetMapper::toDto)
        );
    }
}
