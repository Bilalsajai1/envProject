package ma.perenity.backend.service.impl;

import ma.perenity.backend.dto.MenuDTO;
import ma.perenity.backend.dto.MenuCreateUpdateDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.MenuEntity;
import ma.perenity.backend.mapper.MenuMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.MenuRepository;
import ma.perenity.backend.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final EnvironmentTypeRepository environmentTypeRepository;
    private final MenuMapper mapper;

    public MenuServiceImpl(MenuRepository menuRepository,
                           EnvironmentTypeRepository environmentTypeRepository,
                           MenuMapper mapper) {
        this.menuRepository = menuRepository;
        this.environmentTypeRepository = environmentTypeRepository;
        this.mapper = mapper;
    }

    @Override
    public List<MenuDTO> findAll() {
        return menuRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public MenuDTO findById(Long id) {
        MenuEntity entity = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Menu introuvable"));
        return mapper.toDTO(entity);
    }

    @Override
    public MenuDTO create(MenuCreateUpdateDTO dto) {
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
        MenuEntity entity = menuRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Menu introuvable"));

        menuRepository.delete(entity);
    }
}
