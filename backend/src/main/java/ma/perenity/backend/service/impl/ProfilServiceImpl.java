package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.*;
import ma.perenity.backend.entities.*;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.entities.enums.RoleScope;
import ma.perenity.backend.excepion.BadRequestException;
import ma.perenity.backend.excepion.ErrorMessage;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.ProfilMapper;
import ma.perenity.backend.repository.*;
import ma.perenity.backend.service.KeycloakService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.ProfilService;
import ma.perenity.backend.specification.EntitySpecification;
import ma.perenity.backend.utilities.AdminGuard;
import ma.perenity.backend.utilities.PaginationUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfilServiceImpl implements ProfilService {

    private final ProfilRepository profilRepository;
    private final RoleRepository roleRepository;
    private final ProfilRoleRepository profilRoleRepository;
    private final ProfilMapper mapper;
    private final PermissionService permissionService;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final ProjetRepository projetRepository;
    private final KeycloakService keycloakService;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProfilDTO> getAll() {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        List<ProfilEntity> profils = profilRepository.findByActifTrueAndIsDeletedFalse();

        if (profils.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> profilIds = profils.stream().map(ProfilEntity::getId).toList();

        Map<Long, Integer> userCountMap = profilRepository.countActiveUsersByProfilIds(profilIds).stream()
                .collect(Collectors.toMap(
                        ProfilActiveUserCountView::getProfilId,
                        v -> v.getActiveUserCount().intValue()
                ));

        return profils.stream()
                .map(profil -> mapper.toDto(profil, userCountMap.getOrDefault(profil.getId(), 0)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilDTO getById(Long id) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());
        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));
        return mapper.toDto(profil);
    }

    @Override
    public ProfilDTO create(ProfilCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        if (profilRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException(ErrorMessage.PROFILE_CODE_ALREADY_EXISTS.format(dto.getCode()));
        }

        ProfilEntity profil = ProfilEntity.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .description(dto.getDescription())
                .admin(dto.getAdmin() != null ? dto.getAdmin() : false)
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .isDeleted(false)
                .build();

        ProfilKeycloakDTO keycloakGroupDto = ProfilKeycloakDTO.builder()
                .code(profil.getCode())
                .libelle(profil.getLibelle())
                .roles(Collections.emptyList())
                .build();

        String keycloakGroupId = keycloakService.getOrCreateGroup(keycloakGroupDto);
        profil.setKeycloakGroupId(keycloakGroupId);

        try {
            profil = profilRepository.save(profil);
            return mapper.toDto(profil);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Le code profil existe deja : " + dto.getCode());
        }
    }

    @Override
    public ProfilDTO update(Long id, ProfilCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));

        if (!profil.getCode().equals(dto.getCode())
                && profilRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException(ErrorMessage.PROFILE_CODE_ALREADY_EXISTS.format(dto.getCode()));
        }

        profil.setCode(dto.getCode());
        profil.setLibelle(dto.getLibelle());
        profil.setDescription(dto.getDescription());

        if (dto.getAdmin() != null) {
            profil.setAdmin(dto.getAdmin());
        }
        if (dto.getActif() != null) {
            profil.setActif(dto.getActif());
        }

        return mapper.toDto(profilRepository.save(profil));
    }

    @Override
    public void delete(Long id) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));

        long activeUsers = utilisateurRepository.countByProfil_IdAndActifTrueAndIsDeletedFalse(id);
        if (activeUsers > 0) {
            throw new BadRequestException(ErrorMessage.PROFILE_HAS_ACTIVE_USERS);
        }

        if (profil.getKeycloakGroupId() != null) {
            keycloakService.deleteGroup(profil.getKeycloakGroupId());
        }

        profil.setActif(false);
        profil.setIsDeleted(true);
        profilRepository.save(profil);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRoleIds(Long profilId) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());
        return profilRoleRepository.findRoleIdsByProfilId(profilId);
    }

    @Override
    public void assignRoles(Long profilId, List<Long> roleIds) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));

        profilRoleRepository.deleteByProfilId(profilId);

        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        List<Long> uniqueRoleIds = roleIds.stream().distinct().toList();

        List<RoleEntity> roles = roleRepository.findAllById(uniqueRoleIds);

        if (roles.size() != uniqueRoleIds.size()) {
            throw new BadRequestException(ErrorMessage.ROLE_NOT_FOUND);
        }

        List<ProfilRoleEntity> profilRoles = roles.stream()
                .map(role -> {
                    ProfilRoleEntity pr = new ProfilRoleEntity();
                    pr.setProfil(profil);
                    pr.setRole(role);
                    return pr;
                })
                .toList();

        profilRoleRepository.saveAll(profilRoles);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProfilDTO> search(PaginationRequest req) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        Pageable pageable = PaginationUtils.buildPageable(req);

        Map<String, Object> rawFilters = PaginationUtils.extractFilters(req);
        String search = PaginationUtils.extractSearch(rawFilters);
        boolean hasActifFilter = rawFilters.containsKey("actif");
        rawFilters.remove("onlyActiveUsers");

        EntitySpecification<ProfilEntity> specBuilder = new EntitySpecification<>();
        Specification<ProfilEntity> spec = specBuilder.getSpecification(rawFilters);

        spec = spec.and((root, query, cb) -> cb.isFalse(root.get("isDeleted")));
        if (!hasActifFilter) {
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("actif")));
        }

        if (search != null) {
            final String term = "%" + search.toLowerCase() + "%";

            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), term),
                    cb.like(cb.lower(root.get("libelle")), term),
                    cb.like(cb.lower(root.get("description")), term)
            ));
        }

        Page<ProfilEntity> page = profilRepository.findAll(spec, pageable);

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilPermissionsDTO getPermissions(Long profilId) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));

        if (Boolean.TRUE.equals(profil.getAdmin())) {
            return buildAdminPermissions(profil);
        }

        return buildNormalUserPermissions(profil, profilId);
    }

    private ProfilPermissionsDTO buildAdminPermissions(ProfilEntity profil) {
        List<ActionType> allActions = Arrays.asList(ActionType.values());

        List<EnvironmentTypeEntity> allTypes = environmentTypeRepository.findByActifTrue();

        List<EnvironmentTypePermissionDTO> envPermissions = allTypes.stream()
                .map(type -> EnvironmentTypePermissionDTO.builder()
                        .id(type.getId())
                        .code(type.getCode())
                        .libelle(type.getLibelle())
                        .actif(type.getActif())
                        .allowedActions(new ArrayList<>(allActions))
                        .build())
                .toList();

        List<ProjetEntity> allProjects = projetRepository.findByActifTrueWithEnvironments();

        List<ProjectPermissionDTO> projPermissions = allProjects.stream()
                .map(projet -> {
                    List<String> environmentTypeCodes = getProjectEnvTypeCodes(projet);

                    return ProjectPermissionDTO.builder()
                            .id(projet.getId())
                            .code(projet.getCode())
                            .libelle(projet.getLibelle())
                            .actif(projet.getActif())
                            .allowedActions(new ArrayList<>(allActions))
                            .environmentTypeCodes(environmentTypeCodes)
                            .build();
                })
                .toList();

        return ProfilPermissionsDTO.builder()
                .profilId(profil.getId())
                .profilCode(profil.getCode())
                .profilLibelle(profil.getLibelle())
                .isAdmin(true)
                .environmentTypes(envPermissions)
                .projects(projPermissions)
                .build();
    }

    private ProfilPermissionsDTO buildNormalUserPermissions(ProfilEntity profil, Long profilId) {
        List<RoleEntity> roles = profilRoleRepository.findRolesByProfil(profilId);

        Map<String, Set<ActionType>> envTypeActionsMap = new HashMap<>();
        Map<Long, Set<ActionType>> projectActionsMap = new HashMap<>();

        for (RoleEntity role : roles) {
            String code = role.getCode();
            ActionType action = role.getAction();

            if (code == null || action == null) {
                continue;
            }

            if (code.startsWith("ENV_")) {
                String[] parts = code.split("_");
                if (parts.length >= 3) {
                    String typeCode = parts[1];
                    envTypeActionsMap
                            .computeIfAbsent(typeCode, k -> new HashSet<>())
                            .add(action);
                }
            } else if (code.startsWith("PROJ_")) {
                if (role.getProjet() != null) {
                    projectActionsMap
                            .computeIfAbsent(role.getProjet().getId(), k -> new HashSet<>())
                            .add(action);
                }
            }
        }

        List<EnvironmentTypeEntity> allTypes = environmentTypeRepository.findByActifTrue();
        List<EnvironmentTypePermissionDTO> envPermissions = allTypes.stream()
                .map(type -> {
                    Set<ActionType> actions = envTypeActionsMap.getOrDefault(
                            type.getCode().toUpperCase(), Collections.emptySet());

                    return EnvironmentTypePermissionDTO.builder()
                            .id(type.getId())
                            .code(type.getCode())
                            .libelle(type.getLibelle())
                            .actif(type.getActif())
                            .allowedActions(new ArrayList<>(actions))
                            .build();
                })
                .toList();

        List<ProjetEntity> allProjects = projetRepository.findByActifTrueWithEnvironments();
        List<ProjectPermissionDTO> projPermissions = allProjects.stream()
                .map(projet -> {
                    Set<ActionType> actions = projectActionsMap.getOrDefault(
                            projet.getId(), Collections.emptySet());

                    List<String> environmentTypeCodes = getProjectEnvTypeCodes(projet);

                    return ProjectPermissionDTO.builder()
                            .id(projet.getId())
                            .code(projet.getCode())
                            .libelle(projet.getLibelle())
                            .actif(projet.getActif())
                            .allowedActions(new ArrayList<>(actions))
                            .environmentTypeCodes(environmentTypeCodes)
                            .build();
                })
                .toList();

        return ProfilPermissionsDTO.builder()
                .profilId(profil.getId())
                .profilCode(profil.getCode())
                .profilLibelle(profil.getLibelle())
                .isAdmin(false)
                .environmentTypes(envPermissions)
                .projects(projPermissions)
                .build();
    }

    private List<String> getProjectEnvTypeCodes(ProjetEntity projet) {
        Set<String> codes = new HashSet<>();

        if (projet.getEnvironnements() != null) {
            projet.getEnvironnements().stream()
                    .filter(env -> Boolean.TRUE.equals(env.getActif()) && env.getType() != null && env.getType().getCode() != null)
                    .forEach(env -> codes.add(env.getType().getCode()));
        }

        if (projet.getEnvironmentTypes() != null) {
            projet.getEnvironmentTypes().stream()
                    .filter(et -> et != null && et.getCode() != null)
                    .forEach(et -> codes.add(et.getCode()));
        }

        return codes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .sorted()
                .toList();
    }

    @Override
    public void updateEnvTypePermissions(Long profilId, List<EnvTypePermissionUpdateDTO> envPermissions) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));

        Map<String, Set<ActionType>> requested = (envPermissions != null ? envPermissions : List.<EnvTypePermissionUpdateDTO>of())
                .stream()
                .filter(dto -> dto.getEnvTypeCode() != null)
                .collect(Collectors.toMap(
                        dto -> dto.getEnvTypeCode().trim().toUpperCase(),
                        dto -> {
                            List<ActionType> acts = dto.getActions();
                            if (acts == null || acts.isEmpty()) {
                                return new HashSet<>(Collections.singletonList(ActionType.CONSULT));
                            }
                            return new HashSet<>(acts);
                        },
                        (a, b) -> b
                ));

        profilRoleRepository.deleteByProfilIdAndRoleCodePrefix(profilId, "ENV_");

        List<EnvironmentTypeEntity> envTypes = environmentTypeRepository.findByActifTrue();

        for (EnvironmentTypeEntity type : envTypes) {
            String typeCodeUpper = type.getCode().trim().toUpperCase();

            Set<ActionType> actions = requested.get(typeCodeUpper);
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            for (ActionType action : actions) {
                String roleCode = "ENV_" + typeCodeUpper + "_" + action.name();

                RoleEntity role = roleRepository.findByCode(roleCode)
                        .orElseGet(() -> {
                            RoleEntity newRole = RoleEntity.builder()
                                    .code(roleCode)
                                    .libelle(action.name() + " sur type " + type.getCode())
                                    .action(action)
                                    .scope(RoleScope.ENV_TYPE)
                                    .actif(true)
                                    .build();
                            return roleRepository.save(newRole);
                        });

                ProfilRoleEntity pr = new ProfilRoleEntity();
                pr.setProfil(profil);
                pr.setRole(role);
                profilRoleRepository.save(pr);
            }
        }

        if (profil.getKeycloakGroupId() != null) {
            List<RoleKeycloakDTO> keycloakRoles = profilRoleRepository.findRolesByProfil(profilId)
                    .stream()
                    .filter(r -> r.getCode() != null)
                    .map(r -> RoleKeycloakDTO.builder()
                            .code(r.getCode())
                            .libelle(r.getLibelle())
                            .build())
                    .toList();

            keycloakService.updateGroup(profil.getKeycloakGroupId(), keycloakRoles);
        }
    }

    @Override
    public void updateProjectPermissions(Long profilId, List<ProjectPermissionUpdateDTO> projectPermissions) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));


        Map<Long, Set<ActionType>> requestedById = (projectPermissions != null ? projectPermissions : List.<ProjectPermissionUpdateDTO>of())
                .stream()
                .filter(dto -> dto.getProjectId() != null && dto.getActions() != null)
                .collect(Collectors.toMap(
                        ProjectPermissionUpdateDTO::getProjectId,
                        dto -> new HashSet<>(dto.getActions()),
                        (a, b) -> b
                ));


        profilRoleRepository.deleteByProfilIdAndRoleCodePrefix(profilId, "PROJ_");

        if (requestedById.isEmpty()) {
            return;
        }

        List<ProjetEntity> projects = projetRepository.findByIdInAndActifTrue(requestedById.keySet());

        for (ProjetEntity projet : projects) {
            Set<ActionType> actions = requestedById.get(projet.getId());

            String projectCodeUpper = projet.getCode().trim().toUpperCase();

            for (ActionType action : actions) {
                String roleCode = "PROJ_" + projectCodeUpper + "_" + action.name();


                RoleEntity role = roleRepository.findByCode(roleCode)
                        .orElseGet(() -> {
                            RoleEntity newRole = RoleEntity.builder()
                                    .code(roleCode)
                                    .libelle("Permission " + action.name() + " sur projet " + projet.getCode())
                                    .action(action)
                                    .scope(RoleScope.PROJECT)
                                    .projet(projet)
                                    .actif(true)
                                    .build();
                            return roleRepository.save(newRole);
                        });


                if (role.getProjet() == null) {
                    role.setProjet(projet);
                    role.setScope(RoleScope.PROJECT);
                    role = roleRepository.save(role);
                }

                ProfilRoleEntity pr = new ProfilRoleEntity();
                pr.setProfil(profil);
                pr.setRole(role);
                profilRoleRepository.save(pr);
            }
        }

    }

    @Override
    public void updateAllPermissions(Long profilId, UpdateProfilPermissionsRequest request) {
        AdminGuard.requireAdmin(permissionService, ErrorMessage.PROFILE_ADMIN_REQUIRED.getMessage());

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.PROFILE_NOT_FOUND.getMessage()));

        if (Boolean.TRUE.equals(profil.getAdmin())) {
            throw new BadRequestException(ErrorMessage.PROFILE_ADMIN_PERMISSIONS_IMMUTABLE);
        }

        if (request.getEnvTypePermissions() != null) {
            updateEnvTypePermissions(profilId, request.getEnvTypePermissions());
        }

        if (request.getProjectPermissions() != null) {
            updateProjectPermissions(profilId, request.getProjectPermissions());
        }
    }
}
