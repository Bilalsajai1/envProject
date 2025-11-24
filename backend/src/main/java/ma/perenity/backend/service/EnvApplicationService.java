package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.entities.ApplicationEntity;
import ma.perenity.backend.entities.EnvApplicationEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvApplicationMapper;
import ma.perenity.backend.repository.ApplicationRepository;
import ma.perenity.backend.repository.EnvApplicationRepository;
import ma.perenity.backend.repository.EnvironnementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvApplicationService {

    private final EnvApplicationRepository repository;
    private final EnvironnementRepository environnementRepository;
    private final ApplicationRepository applicationRepository;

    private final EnvApplicationMapper mapper;


    public List<EnvApplicationDTO> getByEnvironnement(Long envId) {

        environnementRepository.findById(envId)
                .orElseThrow(() -> new ResourceNotFoundException("Environnement not found"));

        return repository.findByEnvironnementId(envId)
                .stream()
                .filter(EnvApplicationEntity::getActif) // <- n'affiche que les actives
                .map(mapper::toDto)
                .toList();
    }


    public EnvApplicationDTO create(EnvApplicationDTO dto) {

        EnvironnementEntity env = environnementRepository.findById(dto.getEnvironnementId())
                .orElseThrow(() -> new ResourceNotFoundException("Environnement not found"));

        ApplicationEntity app = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // Vérifier si l’application existe déjà dans cet environnement
        boolean exists = repository.findByEnvironnementId(dto.getEnvironnementId())
                .stream()
                .anyMatch(a -> a.getApplication().getId().equals(dto.getApplicationId()));

        if (exists) {
            throw new IllegalStateException("Cette application existe déjà dans cet environnement.");
        }

        EnvApplicationEntity entity = mapper.toEntity(dto);
        entity.setEnvironnement(env);
        entity.setApplication(app);
        entity.setActif(true);
        entity.setCreatedAt(LocalDateTime.now());

        entity = repository.save(entity);

        return mapper.toDto(entity);
    }


    public EnvApplicationDTO update(Long id, EnvApplicationDTO dto) {

        EnvApplicationEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EnvApplication not found"));

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