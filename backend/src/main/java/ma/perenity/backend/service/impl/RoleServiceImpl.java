package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.RoleCreateUpdateDTO;
import ma.perenity.backend.dto.RoleDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.mapper.RoleMapper;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.repository.RoleRepository;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.RoleService;
import ma.perenity.backend.service.util.AdminGuard;
import ma.perenity.backend.service.util.PaginationUtils;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final EnvironnementRepository environnementRepository;
    private final ProjetRepository projetRepository;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;

    @Override
    public List<RoleDTO> getAll() {
        AdminGuard.requireAdmin(permissionService, "Administration des roles reservee a l'administrateur");
        return roleMapper.toDtoList(roleRepository.findByActifTrue());
    }

    @Override
    public RoleDTO getById(Long id) {
        AdminGuard.requireAdmin(permissionService, "Administration des roles reservee a l'administrateur");
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role introuvable"));
        return roleMapper.toDto(role);
    }

    @Override
    public RoleDTO create(RoleCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des roles reservee a l'administrateur");

        roleRepository.findByCode(dto.getCode()).ifPresent(r -> {
            throw new ResponseStatusException(BAD_REQUEST, "Code de role deja utilise");
        });

        ActionType actionType = parseAction(dto.getAction());

        EnvironnementEntity env = null;
        if (dto.getEnvironnementId() != null) {
            env = environnementRepository.findById(dto.getEnvironnementId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Environnement introuvable : " + dto.getEnvironnementId()));
        }

        ProjetEntity projet = null;
        if (dto.getProjetId() != null) {
            projet = projetRepository.findById(dto.getProjetId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Projet introuvable : " + dto.getProjetId()));
        }

        RoleEntity role = RoleEntity.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .action(actionType)
                .actif(dto.getActif() == null || dto.getActif())
                .environnement(env)
                .projet(projet)
                .build();

        RoleEntity saved = roleRepository.save(role);
        return roleMapper.toDto(saved);
    }

    @Override
    public RoleDTO update(Long id, RoleCreateUpdateDTO dto) {
        AdminGuard.requireAdmin(permissionService, "Administration des roles reservee a l'administrateur");

        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role introuvable"));

        if (!role.getCode().equals(dto.getCode())) {
            roleRepository.findByCode(dto.getCode()).ifPresent(r -> {
                throw new ResponseStatusException(BAD_REQUEST, "Code de role deja utilise");
            });
        }

        ActionType actionType = parseAction(dto.getAction());

        EnvironnementEntity env = null;
        if (dto.getEnvironnementId() != null) {
            env = environnementRepository.findById(dto.getEnvironnementId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Environnement introuvable : " + dto.getEnvironnementId()));
        }

        ProjetEntity projet = null;
        if (dto.getProjetId() != null) {
            projet = projetRepository.findById(dto.getProjetId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Projet introuvable : " + dto.getProjetId()));
        }

        role.setCode(dto.getCode());
        role.setLibelle(dto.getLibelle());
        role.setAction(actionType);
        role.setActif(dto.getActif() == null ? role.getActif() : dto.getActif());
        role.setEnvironnement(env);
        role.setProjet(projet);

        RoleEntity updated = roleRepository.save(role);
        return roleMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        AdminGuard.requireAdmin(permissionService, "Administration des roles reservee a l'administrateur");

        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role introuvable"));

        role.setActif(false);
        roleRepository.save(role);
    }

    @Override
    public List<RoleDTO> getByEnvironnement(Long envId) {
        AdminGuard.requireAdmin(permissionService, "Administration des roles reservee a l'administrateur");
        return roleMapper.toDtoList(roleRepository.findByEnvironnementId(envId));
    }

    @Override
    public PaginatedResponse<RoleDTO> search(PaginationRequest req) {
        AdminGuard.requireAdmin(permissionService, "Administration des roles reservee a l'administrateur");

        Pageable pageable = PaginationUtils.buildPageable(req);
        EntitySpecification<RoleEntity> specBuilder = new EntitySpecification<>();

        Page<RoleEntity> page = roleRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(roleMapper::toDto)
        );
    }

    private ActionType parseAction(String value) {
        try {
            return ActionType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Action invalide : " + value);
        }
    }
}
