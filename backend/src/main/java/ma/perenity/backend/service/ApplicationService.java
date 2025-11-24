package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.entities.ApplicationEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.ApplicationMapper;
import ma.perenity.backend.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repository;
    private final ApplicationMapper mapper;

    public List<ApplicationDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<ApplicationDTO> getAllActive() {
        return repository.findByActifTrue().stream()
                .map(mapper::toDto)
                .toList();
    }

    public ApplicationDTO getById(Long id) {
        ApplicationEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));
        return mapper.toDto(entity);
    }

    public ApplicationDTO create(ApplicationDTO dto) {
        ApplicationEntity entity = mapper.toEntity(dto);
        entity.setActif(true);
        entity.setCreatedAt(LocalDateTime.now());
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    public ApplicationDTO update(Long id, ApplicationDTO dto) {
        ApplicationEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        mapper.updateEntity(entity, dto);
        entity.setUpdatedAt(LocalDateTime.now());
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
