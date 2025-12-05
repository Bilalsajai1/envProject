package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.perenity.backend.dto.*;
import ma.perenity.backend.entities.*;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.entities.enums.RoleScope;
import ma.perenity.backend.mapper.ProfilMapper;
import ma.perenity.backend.repository.*;
import ma.perenity.backend.service.KeycloakService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.ProfilService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
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

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administration des profils réservée à l'administrateur");
        }
    }

    @Override
    public List<ProfilDTO> getAll() {
        checkAdmin();
        return mapper.toDtoList(profilRepository.findByActifTrue());
    }

    @Override
    public ProfilDTO getById(Long id) {
        checkAdmin();
        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));
        return mapper.toDto(profil);
    }

    @Override
    public ProfilDTO create(ProfilCreateUpdateDTO dto) {
        checkAdmin();

        if (profilRepository.existsByCode(dto.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le code profil existe déjà : " + dto.getCode());
        }

        ProfilKeycloakDTO keycloakDto = ProfilKeycloakDTO.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .roles(Collections.emptyList())
                .build();

        String keycloakGroupId;
        try {
            keycloakGroupId = keycloakService.createGroup(keycloakDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la création du groupe Keycloak: " + e.getMessage());
        }

        ProfilEntity profil = ProfilEntity.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .description(dto.getDescription())
                .admin(dto.getAdmin() != null ? dto.getAdmin() : false)
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .keycloakGroupId(keycloakGroupId)
                .build();

        profil = profilRepository.save(profil);
        return mapper.toDto(profil);
    }


    @Override
    public ProfilDTO update(Long id, ProfilCreateUpdateDTO dto) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        // Vérifier unicité du code si changé
        if (!profil.getCode().equals(dto.getCode())
                && profilRepository.existsByCode(dto.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le code profil existe déjà : " + dto.getCode());
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
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        if (profil.getKeycloakGroupId() != null) {
                keycloakService.deleteGroup(profil.getKeycloakGroupId());
        }
        profil.setActif(false);
        profilRepository.save(profil);
    }

    @Override
    public List<Long> getRoleIds(Long profilId) {
        checkAdmin();
        return profilRoleRepository.findRoleIdsByProfilId(profilId);
    }

    @Override
    public void assignRoles(Long profilId, List<Long> roleIds) {
        checkAdmin();

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
        checkAdmin();

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<ProfilEntity> specBuilder = new EntitySpecification<>();

        Page<ProfilEntity> page = profilRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }


    @Override
    @Transactional(readOnly = true)
    public ProfilPermissionsDTO getPermissions(Long profilId) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

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
                    String typeCode = parts[1]; // EDITION, CLIENT, INTEGRATION
                    envTypeActionsMap
                            .computeIfAbsent(typeCode, k -> new HashSet<>())
                            .add(action);


                }
            }

            else if (code.startsWith("PROJ_")) {

                if (role.getProjet() != null) {
                    projectActionsMap
                            .computeIfAbsent(role.getProjet().getId(), k -> new HashSet<>())
                            .add(action);


                } else {

                    String[] parts = code.split("_");
                    if (parts.length >= 3) {

                        StringBuilder projectCodeBuilder = new StringBuilder();
                        for (int i = 1; i < parts.length - 1; i++) {
                            if (i > 1) projectCodeBuilder.append("_");
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


                    return ProjectPermissionDTO.builder()
                            .id(projet.getId())
                            .code(projet.getCode())
                            .libelle(projet.getLibelle())
                            .actif(projet.getActif())
                            .allowedActions(new ArrayList<>(actions))
                            .build();
                })
                .toList();

        return ProfilPermissionsDTO.builder()
                .profilId(profil.getId())
                .profilCode(profil.getCode())
                .profilLibelle(profil.getLibelle())
                .environmentTypes(envPermissions)
                .projects(projPermissions)
                .build();
    }
    @Override
    public void updateEnvTypePermissions(Long profilId, List<EnvTypePermissionUpdateDTO> envPermissions) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));


        Map<String, Set<ActionType>> requested = (envPermissions != null ? envPermissions : List.<EnvTypePermissionUpdateDTO>of())
                .stream()
                .filter(dto -> dto.getEnvTypeCode() != null && dto.getActions() != null)
                .collect(Collectors.toMap(
                        dto -> dto.getEnvTypeCode().trim().toUpperCase(),
                        dto -> new HashSet<>(dto.getActions()),
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
                                    .libelle("Permission " + action.name() + " sur type " + type.getCode())
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
    }

    @Override
    public void updateProjectPermissions(Long profilId, List<ProjectPermissionUpdateDTO> projectPermissions) {
        checkAdmin();

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
        checkAdmin();


        if (request.getEnvTypePermissions() != null) {
            updateEnvTypePermissions(profilId, request.getEnvTypePermissions());
        }

        if (request.getProjectPermissions() != null) {
            updateProjectPermissions(profilId, request.getProjectPermissions());
        }

    }
}