package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.perenity.backend.dto.UserPermissionsDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.RoleEntity;
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
        private UtilisateurEntity user;
        private ProfilEntity profil;
        private boolean admin;
        private Set<String> roleCodes;

        public UtilisateurEntity getUser() {
            return user;
        }

        public void setUser(UtilisateurEntity user) {
            this.user = user;
        }

        public ProfilEntity getProfil() {
            return profil;
        }

        public void setProfil(ProfilEntity profil) {
            this.profil = profil;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public Set<String> getRoleCodes() {
            return roleCodes;
        }

        public void setRoleCodes(Set<String> roleCodes) {
            this.roleCodes = roleCodes;
        }
    }

    private UserContext loadCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
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
                    "Aucun profil associé à l'utilisateur");
        }

        List<RoleEntity> roles = profilRoleRepository.findRolesByProfil(profil.getId());

        Set<String> roleCodes = roles.stream()
                .map(RoleEntity::getCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());


        UserContext ctx = new UserContext();
        ctx.setUser(user);
        ctx.setProfil(profil);
        ctx.setAdmin(Boolean.TRUE.equals(profil.getAdmin()));
        ctx.setRoleCodes(roleCodes);
        return ctx;
    }

    @Override
    public boolean isAdmin() {
        boolean admin = loadCurrentUser().isAdmin();
        return admin;
    }

    @Override
    public boolean hasRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }
        UserContext ctx = loadCurrentUser();
        boolean hasRole = ctx.getRoleCodes().contains(roleCode.trim().toUpperCase());


        return hasRole;
    }

    @Override
    public boolean hasAnyRole(String... roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        UserContext ctx = loadCurrentUser();
        Set<String> userRoles = ctx.getRoleCodes();

        for (String rc : roleCodes) {
            if (rc != null && userRoles.contains(rc.trim().toUpperCase())) {
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

        String type = envTypeCode.trim().toUpperCase();
        String roleToCheck = "ENV_" + type + "_" + action.name();

        boolean hasAccess = ctx.getRoleCodes().contains(roleToCheck);



        return hasAccess;
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

        String code = "PROJ_" + projectCode.trim().toUpperCase() + "_" + action.name();
        boolean hasAccess = ctx.getRoleCodes().contains(code);



        return hasAccess;
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

        String code = "PROJ_" + projet.getCode().trim().toUpperCase() + "_" + action.name();
        boolean hasAccess = ctx.getRoleCodes().contains(code);

        return hasAccess;
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

        String envTypeCode = env.getType().getCode();
        String projectCode = env.getProjet().getCode();

        if (envTypeCode == null || projectCode == null) {
            return false;
        }

        String envRole = "ENV_" + envTypeCode.trim().toUpperCase() + "_" + action.name();
        String projRole = "PROJ_" + projectCode.trim().toUpperCase() + "_" + action.name();

        Set<String> roles = ctx.getRoleCodes();
        boolean hasEnvRole = roles.contains(envRole);
        boolean hasProjRole = roles.contains(projRole);
        boolean hasAccess = hasEnvRole && hasProjRole;

        return hasAccess;
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
}