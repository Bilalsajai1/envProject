package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.UserPermissionsDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.UtilisateurEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.repository.ProfilRoleRepository;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.repository.UtilisateurRepository;
import ma.perenity.backend.service.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRoleRepository profilRoleRepository;
    private final ProjetRepository projetRepository;

    private static class UserContext {
        private final UtilisateurEntity user;
        private final ProfilEntity profil;
        private final boolean admin;
        private final Set<String> roleCodes;

        UserContext(UtilisateurEntity user, ProfilEntity profil, boolean admin, Set<String> roleCodes) {
            this.user = user;
            this.profil = profil;
            this.admin = admin;
            this.roleCodes = roleCodes;
        }

        UtilisateurEntity getUser() {
            return user;
        }

        ProfilEntity getProfil() {
            return profil;
        }

        boolean isAdmin() {
            return admin;
        }

        Set<String> getRoleCodes() {
            return roleCodes;
        }

        boolean hasRole(String roleCode) {
            return roleCodes.contains(roleCode.trim().toUpperCase());
        }
    }

    @Override
    public boolean isAdmin() {
        return loadCurrentUser().isAdmin();
    }

    @Override
    public boolean hasRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }
        return loadCurrentUser().hasRole(roleCode);
    }

    @Override
    public boolean hasAnyRole(String... roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        UserContext ctx = loadCurrentUser();
        for (String rc : roleCodes) {
            if (rc != null && ctx.hasRole(rc)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canAccessEnvType(String envTypeCode, ActionType action) {
        UserContext ctx = loadCurrentUser();

        if (ctx.isAdmin()) {
            return true;
        }

        if (envTypeCode == null || action == null) {
            return false;
        }

        String roleCode = buildEnvTypeRole(envTypeCode, action);
        return ctx.hasRole(roleCode);
    }

    @Override
    public boolean canAccessProjectCode(String projectCode, ActionType action) {
        UserContext ctx = loadCurrentUser();

        if (ctx.isAdmin()) {
            return true;
        }

        if (projectCode == null || action == null) {
            return false;
        }

        String roleCode = buildProjectRole(projectCode, action);
        return ctx.hasRole(roleCode);
    }

    @Override
    public boolean canAccessProject(ProjetEntity projet, ActionType action) {
        if (projet == null || projet.getCode() == null) {
            return false;
        }
        return canAccessProjectCode(projet.getCode(), action);
    }

    @Override
    public boolean canAccessProjectById(Long projectId, ActionType action) {
        UserContext ctx = loadCurrentUser();

        if (ctx.isAdmin()) {
            return true;
        }

        if (projectId == null || action == null) {
            return false;
        }

        ProjetEntity projet = projetRepository.findById(projectId).orElse(null);
        if (projet == null) {
            return false;
        }

        String roleCode = buildProjectRole(projet.getCode(), action);
        return ctx.hasRole(roleCode);
    }

    @Override
    public boolean canAccessEnv(EnvironnementEntity env, ActionType action) {
        UserContext ctx = loadCurrentUser();

        if (ctx.isAdmin()) {
            return true;
        }

        if (env == null || env.getType() == null || env.getProjet() == null || action == null) {
            return false;
        }

        String projectCode = env.getProjet().getCode();
        if (projectCode == null) {
            return false;
        }

        String projRole = buildProjectRole(projectCode, action);
        return ctx.hasRole(projRole);
    }

    @Override
    public UserPermissionsDTO getCurrentUserPermissions() {
        UserContext ctx = loadCurrentUser();
        UtilisateurEntity u = ctx.getUser();
        ProfilEntity p = ctx.getProfil();

        return UserPermissionsDTO.builder()
                .userId(u.getId())
                .code(u.getCode())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .profilCode(p.getCode())
                .profilLibelle(p.getLibelle())
                .admin(ctx.isAdmin())
                .roles(ctx.getRoleCodes())
                .build();
    }

    @Override
    public boolean canViewEnvironmentType(String typeCode) {
        return canAccessEnvType(typeCode, ActionType.CONSULT);
    }

    @Override
    public List<ActionType> getProjectActions(Long projectId) {
        UserContext ctx = loadCurrentUser();

        if (ctx.isAdmin()) {
            return Arrays.asList(ActionType.values());
        }

        if (projectId == null) {
            return Collections.emptyList();
        }

        ProjetEntity projet = projetRepository.findById(projectId).orElse(null);
        if (projet == null) {
            return Collections.emptyList();
        }

        return getProjectActionsByCode(projet.getCode());
    }

    @Override
    public List<ActionType> getProjectActionsByCode(String projectCode) {
        UserContext ctx = loadCurrentUser();

        if (ctx.isAdmin()) {
            return Arrays.asList(ActionType.values());
        }

        if (projectCode == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(ActionType.values())
                .filter(action -> ctx.hasRole(buildProjectRole(projectCode, action)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canConsultProject(Long projectId) {
        return canAccessProjectById(projectId, ActionType.CONSULT);
    }

    @Override
    public boolean canCreateInProject(Long projectId) {
        return canAccessProjectById(projectId, ActionType.CREATE);
    }

    @Override
    public boolean canUpdateInProject(Long projectId) {
        return canAccessProjectById(projectId, ActionType.UPDATE);
    }

    @Override
    public boolean canDeleteInProject(Long projectId) {
        return canAccessProjectById(projectId, ActionType.DELETE);
    }

    private UserContext loadCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifie");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email introuvable dans le token");
        }

        UtilisateurEntity user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Utilisateur introuvable : " + email));

        ProfilEntity profil = user.getProfil();
        if (profil == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Aucun profil associe a l'utilisateur");
        }

        Set<String> roleCodes = profilRoleRepository.findRolesByProfil(profil.getId())
                .stream()
                .map(role -> role.getCode())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        return new UserContext(user, profil, Boolean.TRUE.equals(profil.getAdmin()), roleCodes);
    }

    private String buildEnvTypeRole(String envTypeCode, ActionType action) {
        return "ENV_" + envTypeCode.trim().toUpperCase() + "_" + action.name();
    }

    private String buildProjectRole(String projectCode, ActionType action) {
        return "PROJ_" + projectCode.trim().toUpperCase() + "_" + action.name();
    }
}
