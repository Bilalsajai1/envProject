package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.*;
import ma.perenity.backend.entities.*;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.mapper.ProfilMapper;
import ma.perenity.backend.repository.*;
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

        ProfilEntity profil = ProfilEntity.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .description(dto.getDescription())
                .admin(dto.getAdmin() != null ? dto.getAdmin() : false)
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .build();

        return mapper.toDto(profilRepository.save(profil));
    }

    @Override
    public ProfilDTO update(Long id, ProfilCreateUpdateDTO dto) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role introuvable : " + roleId));

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
    public ProfilPermissionsDTO getPermissions(Long profilId) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        List<RoleEntity> roles = profilRoleRepository.findRolesByProfil(profilId);
        Set<String> roleCodes = roles.stream()
                .map(RoleEntity::getCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        // ----- Env types
        List<EnvironmentTypeEntity> envTypes = environmentTypeRepository.findByActifTrue();

        List<EnvironmentTypePermissionDTO> envPermissions = envTypes.stream()
                .map(t -> {
                    List<ActionType> allowed = Arrays.stream(ActionType.values())
                            .filter(a -> {
                                String code = "ENV_" + t.getCode().trim().toUpperCase() + "_" + a.name();
                                return roleCodes.contains(code);
                            })
                            .toList();

                    return EnvironmentTypePermissionDTO.builder()
                            .id(t.getId())
                            .code(t.getCode())
                            .libelle(t.getLibelle())
                            .actif(t.getActif())
                            .allowedActions(allowed)
                            .build();
                })
                .toList();

        // ----- Projects
        List<ProjetEntity> projets = projetRepository.findAll();

        List<ProjectPermissionDTO> projPermissions = projets.stream()
                .map(p -> {
                    List<ActionType> allowed = Arrays.stream(ActionType.values())
                            .filter(a -> {
                                String code = "PROJ_" + p.getCode().trim().toUpperCase() + "_" + a.name();
                                return roleCodes.contains(code);
                            })
                            .toList();

                    return ProjectPermissionDTO.builder()
                            .id(p.getId())
                            .code(p.getCode())
                            .libelle(p.getLibelle())
                            .actif(p.getActif())
                            .allowedActions(allowed)
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
                .filter(dto -> dto.getEnvTypeCode() != null)
                .collect(Collectors.toMap(
                        dto -> dto.getEnvTypeCode().trim().toUpperCase(),
                        dto -> dto.getActions() == null
                                ? Set.<ActionType>of()
                                : dto.getActions().stream().collect(Collectors.toSet()),
                        (a, b) -> b
                ));

        // On enlève tous les rôles ENV_ pour ce profil
        profilRoleRepository.deleteByProfilIdAndRoleCodePrefix(profilId, "ENV_");

        List<EnvironmentTypeEntity> envTypes = environmentTypeRepository.findAll();

        for (EnvironmentTypeEntity t : envTypes) {
            String typeCodeUpper = t.getCode().trim().toUpperCase();
            Set<ActionType> actions = requested.get(typeCodeUpper);
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            for (ActionType action : actions) {
                String roleCode = "ENV_" + typeCodeUpper + "_" + action.name();

                RoleEntity role = roleRepository.findByCode(roleCode)
                        .orElseGet(() -> roleRepository.save(
                                RoleEntity.builder()
                                        .code(roleCode)
                                        .libelle("Droit " + action.name() + " sur type " + t.getCode())
                                        .action(action)
                                        .actif(true)
                                        .build()
                        ));

                ProfilRoleEntity pr = new ProfilRoleEntity();
                pr.setProfil(profil);
                pr.setRole(role);
                profilRoleRepository.save(pr);
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
                .filter(dto -> dto.getProjectId() != null)
                .collect(Collectors.toMap(
                        ProjectPermissionUpdateDTO::getProjectId,
                        dto -> dto.getActions() == null
                                ? Set.<ActionType>of()
                                : dto.getActions().stream().collect(Collectors.toSet()),
                        (a, b) -> b
                ));

        // On enlève tous les rôles PROJ_ pour ce profil
        profilRoleRepository.deleteByProfilIdAndRoleCodePrefix(profilId, "PROJ_");

        List<ProjetEntity> projects = projetRepository.findAll();

        for (ProjetEntity p : projects) {
            Set<ActionType> actions = requestedById.get(p.getId());
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            String projectCodeUpper = p.getCode().trim().toUpperCase();

            for (ActionType action : actions) {
                String roleCode = "PROJ_" + projectCodeUpper + "_" + action.name();

                RoleEntity role = roleRepository.findByCode(roleCode)
                        .orElseGet(() -> roleRepository.save(
                                RoleEntity.builder()
                                        .code(roleCode)
                                        .libelle("Droit " + action.name() + " sur projet " + p.getCode())
                                        .action(action)
                                        .actif(true)
                                        .build()
                        ));

                ProfilRoleEntity pr = new ProfilRoleEntity();
                pr.setProfil(profil);
                pr.setRole(role);
                profilRoleRepository.save(pr);
            }
        }
    }

}
