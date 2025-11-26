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

    // ============================================================
    // GET ALL ACTIVES
    // ============================================================
    public List<ApplicationDTO> getAll() {
        return repository.findByActifTrue()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================================================
    // GET ALL (ACTIF + INACTIF)
    // ============================================================
    public List<ApplicationDTO> getAllActive() {
        return repository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    // ============================================================
    // GET BY ID (only active)
    // ============================================================
    public ApplicationDTO getById(Long id) {
        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif) // ne renvoyer que les actifs
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        return mapper.toDto(entity);
    }

    // ============================================================
    // CREATE
    // ============================================================
    public ApplicationDTO create(ApplicationDTO dto) {
        ApplicationEntity entity = mapper.toEntity(dto);
        entity.setActif(true);
        entity.setCreatedAt(LocalDateTime.now());

        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public ApplicationDTO update(Long id, ApplicationDTO dto) {

        ApplicationEntity entity = repository.findById(id)
                .filter(ApplicationEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        mapper.updateEntity(entity, dto);

        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);

        return mapper.toDto(entity);
    }

    // ============================================================
    // DELETE LOGIQUE
    // ============================================================
    public void delete(Long id) {

        ApplicationEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id = " + id));

        entity.setActif(false);
        entity.setUpdatedAt(LocalDateTime.now());

        repository.save(entity);
    }
}
