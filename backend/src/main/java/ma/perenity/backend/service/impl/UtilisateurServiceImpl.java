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
import ma.perenity.backend.excepion.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public List<UserDTO> getAll() {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.USER_ADMIN_REQUIRED.getMessage());
        return userMapper.toDtoList(utilisateurRepository.findByActifTrueAndIsDeletedFalse());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.USER_ADMIN_REQUIRED.getMessage());
        return userMapper.toDto(findUserById(id));
    }

    @Override
    public UserDTO create(UserCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.USER_ADMIN_REQUIRED.getMessage());
        validatePassword(dto.getPassword());

        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException(ErrorMessage.USER_EMAIL_ALREADY_EXISTS);
        }

        if (utilisateurRepository.findByCode(dto.getCode()).isPresent()) {
            throw new BadRequestException(ErrorMessage.USER_CODE_ALREADY_EXISTS);
        }

        ProfilEntity profil = findProfilById(dto.getProfilId());
        ensureKeycloakGroupExists(profil);

        String keycloakUserId = null;
        try {
            keycloakUserId = keycloakService.createUser(
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

        } catch (Exception ex) {
            if (keycloakUserId != null) {
                keycloakService.deleteUser(keycloakUserId);
            }
            throw ex;
        }
    }

    @Override
    public UserDTO update(Long id, UserCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.USER_ADMIN_REQUIRED.getMessage());

        UtilisateurEntity entity = findUserById(id);
        ProfilEntity profil = findProfilById(dto.getProfilId());
        ensureKeycloakGroupExists(profil);

        if (entity.getKeycloakId() != null) {
            keycloakService.updateUser(
                    entity.getKeycloakId(),
                    dto.getCode(),
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
                throw new BadRequestException(ErrorMessage.PASSWORD_CHANGE_FAILED_MISSING_KEYCLOAK);
            }
            keycloakService.setPassword(entity.getKeycloakId(), dto.getPassword());
        }

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.USER_ADMIN_REQUIRED.getMessage());

        UtilisateurEntity entity = findUserById(id);

        if (entity.getKeycloakId() != null) {
            try {
                keycloakService.deleteUser(entity.getKeycloakId());
            } catch (Exception ex) {
                throw new BadRequestException(ErrorMessage.KEYCLOAK_USER_DELETION_FAILED);
            }
        }

        entity.setActif(false);
        entity.setIsDeleted(true);
        utilisateurRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<UserDTO> search(PaginationRequest req) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.USER_ADMIN_REQUIRED.getMessage());

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
        AdminGuard.requireAdmin(permissionService, ErrorMessage.USER_ADMIN_REQUIRED.getMessage());
        validatePassword(newPassword);

        UtilisateurEntity entity = findUserById(userId);

        if (entity.getKeycloakId() == null) {
            throw new BadRequestException(ErrorMessage.PASSWORD_CHANGE_FAILED_MISSING_KEYCLOAK);
        }

        keycloakService.setPassword(entity.getKeycloakId(), newPassword);
    }

    @Override
    public void changeOwnPassword(String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BadRequestException(ErrorMessage.CURRENT_PASSWORD_REQUIRED);
        }
        validatePassword(newPassword);

        UtilisateurEntity currentUser = getCurrentAuthenticatedUser();

        if (currentUser.getKeycloakId() == null) {
            throw new BadRequestException(ErrorMessage.PASSWORD_CHANGE_FAILED_MISSING_KEYCLOAK);
        }

        boolean valid = keycloakService.verifyPassword(currentUser.getCode(), currentPassword);
        if (!valid) {
            throw new BadRequestException(ErrorMessage.CURRENT_PASSWORD_INCORRECT);
        }

        keycloakService.setPassword(currentUser.getKeycloakId(), newPassword);
    }

    private UtilisateurEntity findUserById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND.getMessage()));
    }

    private ProfilEntity findProfilById(Long id) {
        return profilRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorMessage.PROFILE_NOT_FOUND));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException(ErrorMessage.PASSWORD_MIN_LENGTH);
        }
    }

    private synchronized void ensureKeycloakGroupExists(ProfilEntity profil) {
        profil = profilRepository.findById(profil.getId()).orElse(profil);

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
            throw new UnauthorizedException(ErrorMessage.USER_NOT_AUTHENTICATED);
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException(ErrorMessage.EMAIL_NOT_FOUND_IN_TOKEN);
        }

        UtilisateurEntity user = utilisateurRepository.findByEmailWithProfil(email)
                .orElseThrow(() -> new UnauthorizedException(ErrorMessage.USER_NOT_FOUND_WITH_EMAIL, email));

        if (Boolean.TRUE.equals(user.getIsDeleted()) || Boolean.FALSE.equals(user.getActif())) {
            throw new ForbiddenException(ErrorMessage.USER_INACTIVE_OR_DELETED);
        }

        return user;
    }
}
