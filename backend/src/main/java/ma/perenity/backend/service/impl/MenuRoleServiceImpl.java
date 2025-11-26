package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.entities.MenuEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.repository.MenuRepository;
import ma.perenity.backend.repository.RoleRepository;
import ma.perenity.backend.service.MenuRoleService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuRoleServiceImpl implements MenuRoleService {

    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<RoleEntity> getRolesForMenu(Long menuId) {
        menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));
        return roleRepository.findRolesByMenu(menuId);
    }

    @Override
    public void assignRoleToMenu(Long menuId, Long roleId) {

        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable"));

        role.setMenu(menu);
        roleRepository.save(role);
    }

    @Override
    public void removeRoleFromMenu(Long menuId, Long roleId) {

        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable"));

        if (role.getMenu() == null || !role.getMenu().getId().equals(menuId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce rôle n'est pas associé à ce menu");
        }

        role.setMenu(null);
        roleRepository.save(role);
    }

    @Override
    public List<RoleEntity> getUnassignedRoles() {
        return roleRepository.findRolesWithoutMenu();
    }

    @Override
    @Transactional
    public void updateRoles(Long menuId, List<Long> roleIds) {

        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));

        // 1. Récupérer tous les rôles actuellement associés à ce menu
        List<RoleEntity> currentRoles = roleRepository.findRolesByMenu(menuId);

        // 2. Désassocier les rôles non présents dans la nouvelle liste
        for (RoleEntity role : currentRoles) {
            if (!roleIds.contains(role.getId())) {
                role.setMenu(null);
                roleRepository.save(role);
            }
        }

        // 3. Associer les nouveaux rôles
        for (Long roleId : roleIds) {
            RoleEntity role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rôle introuvable : " + roleId));
            role.setMenu(menu);
            roleRepository.save(role);
        }
    }
}