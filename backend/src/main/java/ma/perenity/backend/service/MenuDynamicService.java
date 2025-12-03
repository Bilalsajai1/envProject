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


@Service
@RequiredArgsConstructor
public class MenuDynamicService {

    private final MenuRepository menuRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final PermissionService permissionService;
    private final MenuMapper menuMapper;


    @Transactional(readOnly = true)
    public List<MenuDTO> getAccessibleMenusForCurrentUser() {


        if (permissionService.isAdmin()) {
            return menuRepository.findAll()
                    .stream()
                    .filter(MenuEntity::getVisible)
                    .sorted(Comparator.comparing(MenuEntity::getOrdre,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(menuMapper::toDTO)
                    .collect(Collectors.toList());
        }


        List<EnvironmentTypeEntity> accessibleTypes = environmentTypeRepository
                .findByActifTrue()
                .stream()
                .filter(type -> permissionService.canAccessEnvType(
                        type.getCode(), ActionType.CONSULT))
                .toList();

        List<MenuEntity> accessibleMenus = new ArrayList<>();

        for (EnvironmentTypeEntity type : accessibleTypes) {
            List<MenuEntity> menusForType = menuRepository
                    .findByEnvironmentType_CodeAndVisibleTrueOrderByOrdreAsc(
                            type.getCode());


            accessibleMenus.addAll(menusForType);
        }


        return accessibleMenus.stream()
                .distinct()
                .sorted(Comparator.comparing(MenuEntity::getOrdre,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(menuMapper::toDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<MenuDTO> getMenusForEnvironmentType(String typeCode) {

        // Vérifier que l'utilisateur a accès à ce type
        if (!permissionService.isAdmin()
                && !permissionService.canAccessEnvType(typeCode, ActionType.CONSULT)) {
            return List.of();
        }

        return menuRepository
                .findByEnvironmentType_CodeAndVisibleTrueOrderByOrdreAsc(typeCode)
                .stream()
                .map(menuMapper::toDTO)
                .collect(Collectors.toList());
    }
}