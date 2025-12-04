// src/main/java/ma/perenity/backend/service/impl/EnvironnementServiceImpl.java
package ma.perenity.backend.service.impl;

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
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironnementServiceImpl implements EnvironnementService {

    private final EnvironnementRepository environnementRepository;
    private final ProjetRepository projetRepository;
    private final EnvironmentTypeRepository typeRepository;
    private final EnvironnementMapper mapper;
    private final PermissionService permissionService;

    @Override
    public List<EnvironnementDTO> getEnvironmentsByProjetAndType(Long projetId, String typeCode) {

        if (!permissionService.canAccessEnvType(typeCode, ActionType.CONSULT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les environnements de type " + typeCode);
        }

        ProjetEntity projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet not found with id = " + projetId
                ));

        if (!permissionService.canAccessProject(projet, ActionType.CONSULT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les environnements du projet " + projet.getCode());
        }

        return environnementRepository
                .findByProjet_IdAndType_CodeAndActifTrue(projetId, typeCode)
                .stream()
                .map(mapper::toDto)
                .toList();
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de créer des environnements pour le projet " +
                            projet.getCode() + " et le type " + type.getCode());
        }

        EnvironnementEntity env = mapper.toEntity(dto);

        env.setId(null);
        env.setProjet(projet);
        env.setType(type);
        env.setActif(true); // par défaut actif à la création

        env = environnementRepository.save(env);

        return mapper.toDto(env);
    }

    @Override
    public EnvironnementDTO update(Long id, EnvironnementDTO dto) {

        EnvironnementEntity env = getByIdOrThrow(id);

        if (!permissionService.canAccessEnv(env, ActionType.UPDATE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de modifier cet environnement");
        }

        mapper.updateEntityFromDto(dto, env);

        env = environnementRepository.save(env);

        return mapper.toDto(env);
    }

    @Override
    public void delete(Long id) {
        EnvironnementEntity entity = getByIdOrThrow(id);

        if (!permissionService.canAccessEnv(entity, ActionType.DELETE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas le droit de supprimer cet environnement");
        }

        entity.setActif(false);

        environnementRepository.save(entity);
    }

    @Override
    public PaginatedResponse<EnvironnementDTO> search(PaginationRequest req) {

        // Recherche globale réservée à l'admin
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La recherche globale des environnements est réservée à l'administrateur");
        }

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<EnvironnementEntity> specBuilder = new EntitySpecification<>();

        Page<EnvironnementEntity> page = environnementRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
