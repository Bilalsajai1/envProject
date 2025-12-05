package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final KeycloakService keycloakService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administration des utilisateurs réservée à l'administrateur");
        }
    }

    @Override
    public List<UserDTO> getAll() {
        checkAdmin();
        return userMapper.toDtoList(utilisateurRepository.findByActifTrue());
    }

    @Override
    public UserDTO getById(Long id) {
        checkAdmin();
        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return userMapper.toDto(entity);
    }

    @Override
    public UserDTO create(UserCreateUpdateDTO dto) {
        checkAdmin();
        log.info("🔵 Création utilisateur: {}", dto.getCode());

        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le mot de passe est obligatoire et doit contenir au moins 8 caractères");
        }

        ProfilEntity profil = profilRepository.findById(dto.getProfilId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profil introuvable"));

        if (profil.getKeycloakGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le profil n'a pas de groupe Keycloak associé");
        }

        // ✅ Utiliser le bon DTO pour Keycloak
        UtilisateurKeycloakDTO keycloakDto = UtilisateurKeycloakDTO.builder()
                .code(dto.getCode())
                .firstname(dto.getFirstName())
                .lastname(dto.getLastName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .enabled(dto.getActif() != null ? dto.getActif() : true)
                .build();

        String keycloakUserId;
        try {
            keycloakUserId = keycloakService.createUser(keycloakDto, profil.getKeycloakGroupId());
            log.info("✅ Utilisateur créé dans Keycloak avec ID: {}", keycloakUserId);
        } catch (Exception e) {
            log.error("❌ Erreur création Keycloak", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la création dans Keycloak: " + e.getMessage());
        }

        UtilisateurEntity entity = UtilisateurEntity.builder()
                .code(dto.getCode())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .keycloakId(keycloakUserId)
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .profil(profil)
                .build();

        entity = utilisateurRepository.save(entity);
        log.info("✅ Utilisateur sauvegardé en BDD avec ID: {}", entity.getId());

        return userMapper.toDto(entity);
    }

    @Override
    public UserDTO update(Long id, UserCreateUpdateDTO dto) {
        checkAdmin();
        log.info("🔵 Mise à jour utilisateur ID: {}", id);

        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        ProfilEntity profil = profilRepository.findById(dto.getProfilId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profil introuvable"));

        if (profil.getKeycloakGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le profil n'a pas de groupe Keycloak associé");
        }

        if (entity.getKeycloakId() != null) {
            UtilisateurKeycloakDTO keycloakDto = UtilisateurKeycloakDTO.builder()
                    .keycloakId(entity.getKeycloakId())
                    .code(dto.getCode())
                    .firstname(dto.getFirstName())
                    .lastname(dto.getLastName())
                    .email(dto.getEmail())
                    .enabled(dto.getActif() != null ? dto.getActif() : entity.getActif())
                    .build();

            try {
                keycloakService.updateUser(keycloakDto, profil.getKeycloakGroupId());
                log.info("✅ Utilisateur mis à jour dans Keycloak");
            } catch (Exception e) {
                log.error("❌ Erreur mise à jour Keycloak", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erreur lors de la mise à jour dans Keycloak: " + e.getMessage());
            }
        }

        entity.setCode(dto.getCode());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setActif(dto.getActif() != null ? dto.getActif() : entity.getActif());
        entity.setProfil(profil);

        entity = utilisateurRepository.save(entity);
        log.info("✅ Utilisateur mis à jour en BDD");

        return userMapper.toDto(entity);
    }

    @Override
    public void delete(Long id) {
        checkAdmin();
        log.info("🔵 Suppression utilisateur ID: {}", id);

        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (entity.getKeycloakId() != null) {
            try {
                keycloakService.deleteUser(entity.getKeycloakId());
                log.info("✅ Utilisateur supprimé de Keycloak");
            } catch (Exception e) {
                log.warn("⚠️ Impossible de supprimer de Keycloak", e);
            }
        }

        entity.setActif(false);
        utilisateurRepository.save(entity);
        log.info("✅ Utilisateur désactivé en BDD");
    }

    @Override
    public PaginatedResponse<UserDTO> search(PaginationRequest req) {
        checkAdmin();

        Sort sort = req.getSortDirection().equalsIgnoreCase("asc")
                ? Sort.by(req.getSortField()).ascending()
                : Sort.by(req.getSortField()).descending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Map<String, Object> rawFilters = req.getFilters() != null
                ? new HashMap<>(req.getFilters())
                : new HashMap<>();

        String search = null;
        Object searchObj = rawFilters.remove("search");
        if (searchObj != null) {
            search = searchObj.toString().trim();
            if (search.isEmpty()) {
                search = null;
            }
        }

        EntitySpecification<UtilisateurEntity> specBuilder = new EntitySpecification<>();
        Specification<UtilisateurEntity> spec = specBuilder.getSpecification(rawFilters);

        spec = spec.and((root, query, cb) -> cb.equal(root.get("actif"), true));

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
        checkAdmin();
        log.info("🔵 Mise à jour mot de passe utilisateur ID: {}", userId);

        UtilisateurEntity entity = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (entity.getKeycloakId() != null) {
            try {
                keycloakService.updatePassword(entity.getKeycloakId(), newPassword);
                log.info("✅ Mot de passe mis à jour dans Keycloak");
            } catch (Exception e) {
                log.error("❌ Erreur mise à jour mot de passe Keycloak", e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erreur lors de la mise à jour du mot de passe");
            }
        }
    }
}