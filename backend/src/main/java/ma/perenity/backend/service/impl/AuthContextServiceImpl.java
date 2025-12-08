package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.*;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.service.AuthContextService;
import ma.perenity.backend.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthContextServiceImpl implements AuthContextService {

    private final PermissionService permissionService;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final ProjetRepository projetRepository;

    @Override
    public AuthContextDTO getCurrentContext() {
        UserPermissionsDTO user = permissionService.getCurrentUserPermissions();

        List<EnvironmentTypeEntity> envTypes = environmentTypeRepository.findByActifTrue();

        List<EnvironmentTypeWithProjectsDTO> environmentTypes = envTypes.stream()
                .filter(type -> permissionService.canViewEnvironmentType(type.getCode()))
                .map(type -> {
                    List<ProjetEntity> allProjects = projetRepository.findByEnvironmentTypeCode(type.getCode());

                    List<ProjectWithActionsDTO> accessibleProjects = allProjects.stream()
                            .filter(ProjetEntity::getActif)
                            .map(projet -> {
                                List<ma.perenity.backend.entities.enums.ActionType> actions =
                                        permissionService.getProjectActions(projet.getId());

                                if (actions.isEmpty()) {
                                    return null;
                                }

                                return ProjectWithActionsDTO.builder()
                                        .id(projet.getId())
                                        .code(projet.getCode())
                                        .libelle(projet.getLibelle())
                                        .description(projet.getDescription())
                                        .actif(projet.getActif())
                                        .allowedActions(actions)
                                        .build();
                            })
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toList());

                    return EnvironmentTypeWithProjectsDTO.builder()
                            .id(type.getId())
                            .code(type.getCode())
                            .libelle(type.getLibelle())
                            .actif(type.getActif())
                            .projects(accessibleProjects)
                            .build();
                })
                .collect(Collectors.toList());

        return AuthContextDTO.builder()
                .user(user)
                .environmentTypes(environmentTypes)
                .build();
    }
}