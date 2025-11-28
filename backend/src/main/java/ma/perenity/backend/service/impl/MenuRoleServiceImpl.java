package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.RoleDTO;
import ma.perenity.backend.entities.MenuEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.mapper.RoleMapper;
import ma.perenity.backend.repository.MenuRepository;
import ma.perenity.backend.repository.RoleRepository;
import ma.perenity.backend.service.MenuRoleService;
import ma.perenity.backend.service.PermissionService;
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
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administration des associations menu/rôle réservée à l'administrateur");
        }
    }

    @Override
    public List<RoleDTO> getRolesForMenu(Long menuId) {
        checkAdmin();
        menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));
        return roleMapper.toDtoList(roleRepository.findRolesByMenu(menuId));
    }

    @Override
    public void assignRoleToMenu(Long menuId, Long roleId) {
        checkAdmin();

        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rôle introuvable"));

        role.setMenu(menu);
        roleRepository.save(role);
    }

    @Override
    public void removeRoleFromMenu(Long menuId, Long roleId) {
        checkAdmin();

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
    public List<RoleDTO> getUnassignedRoles() {
        checkAdmin();
        return roleMapper.toDtoList(roleRepository.findRolesWithoutMenu());
    }

    @Override
    public void updateRoles(Long menuId, List<Long> roleIds) {
        checkAdmin();

        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu introuvable"));

        List<RoleEntity> currentRoles = roleRepository.findRolesByMenu(menuId);

        for (RoleEntity role : currentRoles) {
            if (!roleIds.contains(role.getId())) {
                role.setMenu(null);
                roleRepository.save(role);
            }
        }

        for (Long roleId : roleIds) {
            RoleEntity role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rôle introuvable : " + roleId));
            role.setMenu(menu);
            roleRepository.save(role);
        }
    }
}
