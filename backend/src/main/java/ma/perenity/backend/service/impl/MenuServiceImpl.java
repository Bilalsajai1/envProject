package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.MenuDTO;
import ma.perenity.backend.dto.MenuCreateUpdateDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.MenuEntity;
import ma.perenity.backend.entities.enums.ActionType;
import ma.perenity.backend.mapper.MenuMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.MenuRepository;
import ma.perenity.backend.service.MenuService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final MenuMapper mapper;
    private final PermissionService permissionService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(FORBIDDEN, "Administration des menus réservée au profil ADMIN");
        }
    }

    @Override
    public List<MenuDTO> findAll() {
        checkAdmin();
        return menuRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public MenuDTO findById(Long id) {
        checkAdmin();
        MenuEntity entity = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Menu introuvable"));
        return mapper.toDTO(entity);
    }

    @Override
    public MenuDTO create(MenuCreateUpdateDTO dto) {
        checkAdmin();

        MenuEntity entity = mapper.toEntity(dto);

        if (dto.getParentId() != null) {
            MenuEntity parent = menuRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parent introuvable"));
            entity.setParent(parent);
        }

        if (dto.getEnvironmentTypeId() != null) {
            EnvironmentTypeEntity envType = environmentTypeRepository.findById(dto.getEnvironmentTypeId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Type d'environnement introuvable"));
            entity.setEnvironmentType(envType);
        }

        return mapper.toDTO(menuRepository.save(entity));
    }

    @Override
    public MenuDTO update(Long id, MenuCreateUpdateDTO dto) {
        checkAdmin();

        MenuEntity entity = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Menu introuvable"));

        mapper.updateEntity(dto, entity);

        if (dto.getParentId() != null) {
            MenuEntity parent = menuRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Parent introuvable"));
            entity.setParent(parent);
        }

        if (dto.getEnvironmentTypeId() != null) {
            EnvironmentTypeEntity envType = environmentTypeRepository.findById(dto.getEnvironmentTypeId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Type d'environnement introuvable"));
            entity.setEnvironmentType(envType);
        }

        return mapper.toDTO(menuRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        checkAdmin();

        MenuEntity entity = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Menu introuvable"));

        if (entity.getRoles() != null && !entity.getRoles().isEmpty()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Impossible de supprimer ce menu : des rôles y sont encore associés. " +
                            "Supprime ou détache d'abord les rôles de ce menu."
            );
        }

        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Impossible de supprimer ce menu : il possède encore des sous-menus."
            );
        }

        menuRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDTO> findByEnvironmentTypeCode(String envTypeCode) {

        // Menus visibles selon droits de CONSULT sur le type
        if (!permissionService.canAccessEnvType(envTypeCode, ActionType.CONSULT)) {
            throw new ResponseStatusException(FORBIDDEN,
                    "Vous n'avez pas le droit de consulter les menus pour le type " + envTypeCode);
        }

        return menuRepository
                .findByEnvironmentType_CodeAndVisibleTrueOrderByOrdreAsc(envTypeCode)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public PaginatedResponse<MenuDTO> search(PaginationRequest req) {
        checkAdmin();

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<MenuEntity> specBuilder = new EntitySpecification<>();

        Page<MenuEntity> page = menuRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDTO)
        );
    }
}
