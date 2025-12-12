package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvTypePermissionUpdateDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProfilCreateUpdateDTO;
import ma.perenity.backend.dto.ProfilDTO;
import ma.perenity.backend.dto.ProfilKeycloakDTO;
import ma.perenity.backend.dto.ProfilPermissionsDTO;
import ma.perenity.backend.dto.ProjectPermissionDTO;
import ma.perenity.backend.dto.ProjectPermissionUpdateDTO;
import ma.perenity.backend.dto.UpdateProfilPermissionsRequest;
import ma.perenity.backend.dto.EnvironmentTypePermissionDTO;
import ma.perenity.backend.dto.RoleKeycloakDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.ProfilRoleEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.entities.enums.RoleScope;
import ma.perenity.backend.mapper.ProfilMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.ProfilRepository;
import ma.perenity.backend.repository.ProfilRoleRepository;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.repository.RoleRepository;
import ma.perenity.backend.repository.UtilisateurRepository;
import ma.perenity.backend.service.KeycloakService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.ProfilService;
import ma.perenity.backend.service.util.AdminGuard;
import ma.perenity.backend.service.util.PaginationUtils;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    public List<ProfilDTO> getAll() {
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");
        return mapper.toDtoList(profilRepository.findByActifTrueAndIsDeletedFalse());

    }

    @Override
    public ProfilDTO getById(Long id) {
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");
        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));
        return mapper.toDto(profil);
    }

    @Override
    public ProfilDTO create(ProfilCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        if (profilRepository.existsByCode(dto.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le code profil existe deja : " + dto.getCode());
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

        profil = profilRepository.save(profil);
        return mapper.toDto(profil);
    }

    @Override
    public ProfilDTO update(Long id, ProfilCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        if (!profil.getCode().equals(dto.getCode())
                && profilRepository.existsByCode(dto.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le code profil existe deja : " + dto.getCode());
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
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        long activeUsers = utilisateurRepository.countByProfil_IdAndActifTrueAndIsDeletedFalse(id);
        if (activeUsers > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de supprimer ce profil : des utilisateurs actifs y sont associes."
            );
        }

        if (profil.getKeycloakGroupId() != null) {
            keycloakService.deleteGroup(profil.getKeycloakGroupId());
        }

        profil.setActif(false);
        profil.setIsDeleted(true);
        profilRepository.save(profil);
    }

    @Override
    public List<Long> getRoleIds(Long profilId) {
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");
        return profilRoleRepository.findRoleIdsByProfilId(profilId);
    }

    @Override
    public void assignRoles(Long profilId, List<Long> roleIds) {
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        profilRoleRepository.deleteByProfilId(profilId);

        for (Long roleId : roleIds) {
            RoleEntity role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Role introuvable : " + roleId));

            ProfilRoleEntity pr = new ProfilRoleEntity();
            pr.setProfil(profil);
            pr.setRole(role);
            profilRoleRepository.save(pr);
        }
    }

    @Override
    public PaginatedResponse<ProfilDTO> search(PaginationRequest req) {
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

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
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

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

        List<ProjetEntity> allProjects = projetRepository.findByActifTrue();

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
                } else {
                    String[] parts = code.split("_");
                    if (parts.length >= 3) {
                        StringBuilder projectCodeBuilder = new StringBuilder();
                        for (int i = 1; i < parts.length - 1; i++) {
                            if (i > 1) {
                                projectCodeBuilder.append("_");
                            }
                            projectCodeBuilder.append(parts[i]);
                        }
                        String projectCode = projectCodeBuilder.toString();

                        projetRepository.findAll().stream()
                                .filter(p -> p.getCode().equalsIgnoreCase(projectCode))
                                .findFirst()
                                .ifPresent(projet -> {
                                    projectActionsMap
                                            .computeIfAbsent(projet.getId(), k -> new HashSet<>())
                                            .add(action);
                                });
                    }
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

        List<ProjetEntity> allProjects = projetRepository.findByActifTrue();
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
                    .filter(env -> env.getActif() && env.getType() != null && env.getType().getCode() != null)
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
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

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
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));


        Map<Long, Set<ActionType>> requestedById = (projectPermissions != null ? projectPermissions : List.<ProjectPermissionUpdateDTO>of())
                .stream()
                .filter(dto -> dto.getProjectId() != null && dto.getActions() != null)
                .collect(Collectors.toMap(
                        ProjectPermissionUpdateDTO::getProjectId,
                        dto -> new HashSet<>(dto.getActions()),
                        (a, b) -> b
                ));


        profilRoleRepository.deleteByProfilIdAndRoleCodePrefix(profilId, "PROJ_");

        List<ProjetEntity> projects = projetRepository.findByActifTrue();

        for (ProjetEntity projet : projects) {
            Set<ActionType> actions = requestedById.get(projet.getId());

            if (actions == null || actions.isEmpty()) {
                continue;
            }

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
        AdminGuard.requireAdmin(permissionService, "Administration des profils reservee a l'administrateur");

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        if (Boolean.TRUE.equals(profil.getAdmin())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de modifier les permissions d'un profil administrateur. " +
                            "Les administrateurs ont automatiquement tous les droits."
            );
        }

        if (request.getEnvTypePermissions() != null) {
            updateEnvTypePermissions(profilId, request.getEnvTypePermissions());
        }

        if (request.getProjectPermissions() != null) {
            updateProjectPermissions(profilId, request.getProjectPermissions());
        }
    }
}
