package ma.perenity.backend.service;

import ma.perenity.backend.dto.RoleDTO;
import ma.perenity.backend.entities.RoleEntity;

import java.util.List;

public interface MenuRoleService {

    List<RoleDTO> getRolesForMenu(Long menuId);

    void assignRoleToMenu(Long menuId, Long roleId);

    void removeRoleFromMenu(Long menuId, Long roleId);

    List<RoleDTO> getUnassignedRoles();

    void updateRoles(Long menuId, List<Long> roleIds);
}
