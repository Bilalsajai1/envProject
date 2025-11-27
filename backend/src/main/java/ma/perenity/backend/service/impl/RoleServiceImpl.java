package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.RoleCreateUpdateDTO;
import ma.perenity.backend.dto.RoleDTO;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.MenuEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.mapper.RoleMapper;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.repository.MenuRepository;
import ma.perenity.backend.repository.RoleRepository;
import ma.perenity.backend.service.RoleService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final EnvironnementRepository environnementRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleDTO> getAll() {
        return roleMapper.toDtoList(roleRepository.findByActifTrue());
    }

    @Override
    public RoleDTO getById(Long id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Rôle introuvable"));
        return roleMapper.toDto(role);
    }

    @Override
    public RoleDTO create(RoleCreateUpdateDTO dto) {
        // unicité sur code
        roleRepository.findByCode(dto.getCode()).ifPresent(r -> {
            throw new ResponseStatusException(BAD_REQUEST, "Code de rôle déjà utilisé");
        });

        ActionType actionType;
        try {
            actionType = ActionType.valueOf(dto.getAction());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Action invalide : " + dto.getAction());
        }

        MenuEntity menu = null;
        if (dto.getMenuId() != null) {
            menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Menu introuvable : " + dto.getMenuId()));
        }

        EnvironnementEntity env = null;
        if (dto.getEnvironnementId() != null) {
            env = environnementRepository.findById(dto.getEnvironnementId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Environnement introuvable : " + dto.getEnvironnementId()));
        }

        RoleEntity role = RoleEntity.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .action(actionType)
                .actif(dto.getActif() == null ? true : dto.getActif())
                .menu(menu)
                .environnement(env)
                .build();

        RoleEntity saved = roleRepository.save(role);
        return roleMapper.toDto(saved);
    }

    @Override
    public RoleDTO update(Long id, RoleCreateUpdateDTO dto) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Rôle introuvable"));

        // si le code change → vérifier unicité
        if (!role.getCode().equals(dto.getCode())) {
            roleRepository.findByCode(dto.getCode()).ifPresent(r -> {
                throw new ResponseStatusException(BAD_REQUEST, "Code de rôle déjà utilisé");
            });
        }

        ActionType actionType;
        try {
            actionType = ActionType.valueOf(dto.getAction());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Action invalide : " + dto.getAction());
        }

        MenuEntity menu = null;
        if (dto.getMenuId() != null) {
            menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Menu introuvable : " + dto.getMenuId()));
        }

        EnvironnementEntity env = null;
        if (dto.getEnvironnementId() != null) {
            env = environnementRepository.findById(dto.getEnvironnementId())
                    .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST,
                            "Environnement introuvable : " + dto.getEnvironnementId()));
        }

        role.setCode(dto.getCode());
        role.setLibelle(dto.getLibelle());
        role.setAction(actionType);
        role.setActif(dto.getActif() == null ? role.getActif() : dto.getActif());
        role.setMenu(menu);
        role.setEnvironnement(env);

        RoleEntity updated = roleRepository.save(role);
        return roleMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Rôle introuvable"));

        role.setActif(false);
        roleRepository.save(role);
    }

    @Override
    public List<RoleDTO> getByMenu(Long menuId) {
        return roleMapper.toDtoList(roleRepository.findByMenuId(menuId));
    }

    @Override
    public List<RoleDTO> getByEnvironnement(Long envId) {
        return roleMapper.toDtoList(roleRepository.findByEnvironnementId(envId));
    }

    @Override
    public PaginatedResponse<RoleDTO> search(PaginationRequest req) {

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<RoleEntity> specBuilder = new EntitySpecification<>();

        Page<RoleEntity> page = roleRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(roleMapper::toDto)
        );
    }
}
