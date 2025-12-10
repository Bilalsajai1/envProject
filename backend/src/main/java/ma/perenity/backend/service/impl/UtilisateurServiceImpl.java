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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
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
        checkAdminPermission();
        return userMapper.toDtoList(utilisateurRepository.findByActifTrueAndIsDeletedFalse());
    }

    @Override
    public UserDTO getById(Long id) {
        checkAdminPermission();
        return userMapper.toDto(findUserById(id));
    }

    @Override
    public UserDTO create(UserCreateUpdateDTO dto) {
        checkAdminPermission();
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
        checkAdminPermission();

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

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        checkAdminPermission();

        UtilisateurEntity entity = findUserById(id);

        if (entity.getKeycloakId() != null) {
            try {
                keycloakService.deleteUser(entity.getKeycloakId());
            } catch (Exception ignored) {}
        }

        entity.setActif(false);
        entity.setIsDeleted(true);
        utilisateurRepository.save(entity);
    }


    @Override
    public PaginatedResponse<UserDTO> search(PaginationRequest req) {
        checkAdminPermission();

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
        checkAdminPermission();
        validatePassword(newPassword);

        UtilisateurEntity entity = findUserById(userId);

        if (entity.getKeycloakId() != null) {
            keycloakService.setPassword(entity.getKeycloakId(), newPassword);
        }
    }

    private void checkAdminPermission() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administration des utilisateurs réservée à l'administrateur");
        }
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
                    "Le mot de passe doit contenir au moins 8 caractères");
        }
    }

    private void ensureKeycloakGroupExists(ProfilEntity profil) {
        if (profil.getKeycloakGroupId() == null) {
            System.out.println("🔍 Profil " + profil.getCode() + " sans keycloakGroupId, recherche/création du groupe...");

            ProfilKeycloakDTO keycloakGroupDto = ProfilKeycloakDTO.builder()
                    .code(profil.getCode())
                    .libelle(profil.getLibelle())
                    .roles(Collections.emptyList())
                    .build();

            // ✅ Utiliser getOrCreateGroup au lieu de createGroup
            String keycloakGroupId = keycloakService.getOrCreateGroup(keycloakGroupDto);

            profil.setKeycloakGroupId(keycloakGroupId);
            profilRepository.save(profil);

            System.out.println("✅ Profil " + profil.getCode() + " associé au groupe Keycloak ID: " + keycloakGroupId);
        } else {
            System.out.println("✅ Profil " + profil.getCode() + " déjà associé au groupe Keycloak ID: " + profil.getKeycloakGroupId());
        }
    }
}