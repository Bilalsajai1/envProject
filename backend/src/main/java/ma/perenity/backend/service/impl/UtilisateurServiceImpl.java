package ma.perenity.backend.service.impl;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.*;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.UtilisateurEntity;
import ma.perenity.backend.mapper.UserMapper;
import ma.perenity.backend.repository.ProfilRepository;
import ma.perenity.backend.repository.UtilisateurRepository;
import ma.perenity.backend.service.KeycloakService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.UtilisateurService;
import ma.perenity.backend.specification.EntitySpecification;
import ma.perenity.backend.utilities.AdminGuard;
import ma.perenity.backend.utilities.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final KeycloakService keycloakService;

    @Override
    public List<UserDTO> getAll() {
        AdminGuard.requireAdmin(permissionService, "Administration des utilisateurs reservee a l'administrateur");
        return userMapper.toDtoList(utilisateurRepository.findByActifTrueAndIsDeletedFalse());
    }

    @Override
    public UserDTO getById(Long id) {
        AdminGuard.requireAdmin(permissionService, "Administration des utilisateurs reservee a l'administrateur");
        return userMapper.toDto(findUserById(id));
    }

    @Override
    public UserDTO create(UserCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des utilisateurs reservee a l'administrateur");
        validatePassword(dto.getPassword());

        ProfilEntity profil = findProfilById(dto.getProfilId());
        ensureKeycloakGroupExists(profil);

        String keycloakUserId = keycloakService.createUser(
                dto.getCode(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getActif() != null ? dto.getActif() : true,
                profil.getKeycloakGroupId()
        );

        UtilisateurEntity entity = UtilisateurEntity.builder()
                .code(dto.getCode())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .keycloakId(keycloakUserId)
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .profil(profil)
                .isDeleted(false)
                .build();

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public UserDTO update(Long id, UserCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des utilisateurs reservee a l'administrateur");

        UtilisateurEntity entity = findUserById(id);
        ProfilEntity profil = findProfilById(dto.getProfilId());
        ensureKeycloakGroupExists(profil);

        if (entity.getKeycloakId() != null) {
            keycloakService.updateUser(
                    entity.getKeycloakId(),
                    entity.getCode(),
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getEmail(),
                    dto.getActif() != null ? dto.getActif() : true,
                    profil.getKeycloakGroupId()
            );
        }

        entity.setCode(dto.getCode());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setActif(dto.getActif() != null ? dto.getActif() : entity.getActif());
        entity.setProfil(profil);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            validatePassword(dto.getPassword());
            if (entity.getKeycloakId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Impossible de mettre a jour le mot de passe : compte Keycloak manquant");
            }
            keycloakService.setPassword(entity.getKeycloakId(), dto.getPassword());
        }

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        AdminGuard.requireAdmin(permissionService, "Administration des utilisateurs reservee a l'administrateur");

        UtilisateurEntity entity = findUserById(id);

        if (entity.getKeycloakId() != null) {
            try {
                keycloakService.deleteUser(entity.getKeycloakId());
            } catch (Exception ignored) {
            }
        }

        entity.setActif(false);
        entity.setIsDeleted(true);
        utilisateurRepository.save(entity);
    }

    @Override
    public PaginatedResponse<UserDTO> search(PaginationRequest req) {
        AdminGuard.requireAdmin(permissionService, "Administration des utilisateurs reservee a l'administrateur");

        Pageable pageable = PaginationUtils.buildPageable(req);
        Map<String, Object> rawFilters = PaginationUtils.extractFilters(req);
        String search = PaginationUtils.extractSearch(rawFilters);

        EntitySpecification<UtilisateurEntity> specBuilder = new EntitySpecification<>();
        Specification<UtilisateurEntity> spec = specBuilder.getSpecification(rawFilters);

        spec = spec.and((root, query, cb) -> cb.isFalse(root.get("isDeleted")));

        if (search != null) {
            final String term = "%" + search.toLowerCase() + "%";

            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> profilJoin = root.join("profil", JoinType.LEFT);

                return cb.or(
                        cb.like(cb.lower(root.get("code")), term),
                        cb.like(cb.lower(root.get("firstName")), term),
                        cb.like(cb.lower(root.get("lastName")), term),
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(profilJoin.get("libelle")), term)
                );
            });
        }

        Page<UtilisateurEntity> page = utilisateurRepository.findAll(spec, pageable);

        return PaginatedResponse.fromPage(page.map(userMapper::toDto));
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        AdminGuard.requireAdmin(permissionService, "Administration des utilisateurs reservee a l'administrateur");
        validatePassword(newPassword);

        UtilisateurEntity entity = findUserById(userId);

        if (entity.getKeycloakId() != null) {
            keycloakService.setPassword(entity.getKeycloakId(), newPassword);
        }
    }

    @Override
    public void changeOwnPassword(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe actuel est requis");
        }
        validatePassword(newPassword);

        UtilisateurEntity currentUser = getCurrentAuthenticatedUser();

        if (currentUser.getKeycloakId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Impossible de changer le mot de passe : compte Keycloak manquant");
        }

        boolean valid = keycloakService.verifyPassword(currentUser.getCode(), currentPassword);
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }

        keycloakService.setPassword(currentUser.getKeycloakId(), newPassword);
    }

    private UtilisateurEntity findUserById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private ProfilEntity findProfilById(Long id) {
        return profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profil introuvable"));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le mot de passe doit contenir au moins 8 caracteres");
        }
    }

    private void ensureKeycloakGroupExists(ProfilEntity profil) {
        if (profil.getKeycloakGroupId() == null) {
            ProfilKeycloakDTO keycloakGroupDto = ProfilKeycloakDTO.builder()
                    .code(profil.getCode())
                    .libelle(profil.getLibelle())
                    .roles(Collections.emptyList())
                    .build();

            String keycloakGroupId = keycloakService.getOrCreateGroup(keycloakGroupDto);

            profil.setKeycloakGroupId(keycloakGroupId);
            profilRepository.save(profil);
        }
    }

    private UtilisateurEntity getCurrentAuthenticatedUser() {
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

        if (Boolean.TRUE.equals(user.getIsDeleted()) || Boolean.FALSE.equals(user.getActif())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utilisateur inactif ou supprime");
        }

        return user;
    }
}
