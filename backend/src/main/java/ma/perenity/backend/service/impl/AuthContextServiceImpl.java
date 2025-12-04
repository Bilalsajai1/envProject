// src/main/java/ma/perenity/backend/service/impl/AuthContextServiceImpl.java
package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.AuthContextDTO;
import ma.perenity.backend.dto.EnvironmentTypePermissionDTO;
import ma.perenity.backend.dto.UserPermissionsDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.service.AuthContextService;
import ma.perenity.backend.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthContextServiceImpl implements AuthContextService {

    private final PermissionService permissionService;
    private final EnvironmentTypeRepository environmentTypeRepository;

    @Override
    public AuthContextDTO getCurrentContext() {

        UserPermissionsDTO user = permissionService.getCurrentUserPermissions();

        List<EnvironmentTypeEntity> envTypes = environmentTypeRepository.findByActifTrue();

        List<EnvironmentTypePermissionDTO> envPermissions = envTypes.stream()
                .map(t -> {
                    List<ActionType> allowedActions = Arrays.stream(ActionType.values())
                            .filter(a -> permissionService.canAccessEnvType(t.getCode(), a))
                            .toList();

                    return EnvironmentTypePermissionDTO.builder()
                            .id(t.getId())
                            .code(t.getCode())
                            .libelle(t.getLibelle())
                            .actif(t.getActif())
                            .allowedActions(allowedActions)
                            .build();
                })
                .toList();

        return AuthContextDTO.builder()
                .user(user)
                .environmentTypes(envPermissions)
                .build();
    }
}
