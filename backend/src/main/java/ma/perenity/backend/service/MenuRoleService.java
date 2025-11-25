package ma.perenity.backend.service;

import ma.perenity.backend.entities.RoleEntity;

import java.util.List;

public interface MenuRoleService {

    List<RoleEntity> getRolesForMenu(Long menuId);

    void assignRoleToMenu(Long menuId, Long roleId);

    void removeRoleFromMenu(Long menuId, Long roleId);

    List<RoleEntity> getUnassignedRoles();

    void updateRoles(Long menuId, List<Long> roleIds);
}
