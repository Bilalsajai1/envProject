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
    public void updateRoles(Long menuId, List<Long> roleIds) {

        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));

        // Retirer tous les rôles actuels
        List<RoleEntity> current = roleRepository.findRolesByMenu(menuId);
        for (RoleEntity r : current) {
            r.setMenu(null);
            roleRepository.save(r);
        }

        // Assigner les nouveaux rôles
        for (Long roleId : roleIds) {
            RoleEntity r = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable"));
            r.setMenu(menu);
            roleRepository.save(r);
        }
    }

}
