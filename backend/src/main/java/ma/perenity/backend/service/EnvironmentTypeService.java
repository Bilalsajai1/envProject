package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvironmentTypeMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironmentTypeService {

    private final EnvironmentTypeRepository repository;
    private final EnvironmentTypeMapper mapper;

    public List<EnvironmentTypeDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<EnvironmentTypeDTO> getAllActive() {
        return repository.findByActifTrue().stream()
                .map(mapper::toDto)
                .toList();
    }

    public EnvironmentTypeDTO getById(Long id) {
        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EnvironmentType not found with id = " + id));
        return mapper.toDto(entity);
    }

    public EnvironmentTypeDTO create(EnvironmentTypeDTO dto) {
        EnvironmentTypeEntity entity = mapper.toEntity(dto);
        entity.setActif(true);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    public EnvironmentTypeDTO update(Long id, EnvironmentTypeDTO dto) {
        EnvironmentTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EnvironmentType not found with id = " + id));
        mapper.updateEntity(entity, dto);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id = " + id);
        }
        repository.deleteById(id);
    }
}
