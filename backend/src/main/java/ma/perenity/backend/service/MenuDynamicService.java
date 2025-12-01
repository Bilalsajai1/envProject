package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.perenity.backend.dto.MenuDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.MenuEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.mapper.MenuMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuDynamicService {

    private final MenuRepository menuRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final PermissionService permissionService;
    private final MenuMapper menuMapper;

    /**
     * Récupère les menus accessibles pour l'utilisateur connecté
     * basé sur ses permissions
     */
    @Transactional(readOnly = true)
    public List<MenuDTO> getAccessibleMenusForCurrentUser() {

        // Si admin, retourner tous les menus
        if (permissionService.isAdmin()) {
            log.debug("Utilisateur admin - retourne tous les menus");
            return menuRepository.findAll()
                    .stream()
                    .filter(MenuEntity::getVisible)
                    .sorted(Comparator.comparing(MenuEntity::getOrdre,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(menuMapper::toDTO)
                    .collect(Collectors.toList());
        }

        // Pour les utilisateurs non-admin, filtrer selon les permissions
        List<EnvironmentTypeEntity> accessibleTypes = environmentTypeRepository
                .findByActifTrue()
                .stream()
                .filter(type -> permissionService.canAccessEnvType(
                        type.getCode(), ActionType.CONSULT))
                .toList();

        log.debug("Types d'environnement accessibles : {}",
                accessibleTypes.stream()
                        .map(EnvironmentTypeEntity::getCode)
                        .collect(Collectors.joining(", ")));

        // Récupérer les menus pour ces types
        List<MenuEntity> accessibleMenus = new ArrayList<>();

        for (EnvironmentTypeEntity type : accessibleTypes) {
            List<MenuEntity> menusForType = menuRepository
                    .findByEnvironmentType_CodeAndVisibleTrueOrderByOrdreAsc(
                            type.getCode());

            log.debug("Menus trouvés pour le type {} : {}",
                    type.getCode(), menusForType.size());

            accessibleMenus.addAll(menusForType);
        }

        // Supprimer les doublons et trier
        return accessibleMenus.stream()
                .distinct()
                .sorted(Comparator.comparing(MenuEntity::getOrdre,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(menuMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les menus pour un type d'environnement spécifique
     * (avec vérification de permission)
     */
    @Transactional(readOnly = true)
    public List<MenuDTO> getMenusForEnvironmentType(String typeCode) {

        // Vérifier que l'utilisateur a accès à ce type
        if (!permissionService.isAdmin()
                && !permissionService.canAccessEnvType(typeCode, ActionType.CONSULT)) {
            log.warn("Accès refusé au type {} pour l'utilisateur", typeCode);
            return List.of();
        }

        return menuRepository
                .findByEnvironmentType_CodeAndVisibleTrueOrderByOrdreAsc(typeCode)
                .stream()
                .map(menuMapper::toDTO)
                .collect(Collectors.toList());
    }
}