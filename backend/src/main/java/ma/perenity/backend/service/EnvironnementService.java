package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvironnementMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.repository.ProjetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvironnementService {

    private final EnvironnementRepository environnementRepository;
    private final ProjetRepository projetRepository;
    private final EnvironmentTypeRepository typeRepository;
    private final EnvironnementMapper mapper;

    // =====================================================
    // GET : environnements actifs par projet + type (via type.code)
    // =====================================================

    public List<EnvironnementDTO> getEnvironmentsByProjetAndType(Long projetId, String typeCode) {

        return environnementRepository.findByProjetAndType(projetId, typeCode)
                .stream()
                .filter(EnvironnementEntity::getActif) // garder juste les actifs
                .map(mapper::toDto)
                .toList();
    }

    public EnvironnementEntity getByIdOrThrow(Long id) {
        return environnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Environnement not found with id = " + id));
    }

    // =====================================================
    // CREATE
    // =====================================================

    public EnvironnementDTO create(EnvironnementDTO dto) {

        ProjetEntity projet = projetRepository.findById(dto.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Projet not found with id = " + dto.getProjetId()));

        // recherche via typeCode
        EnvironmentTypeEntity type = typeRepository.findByCode(dto.getTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Type not found: " + dto.getTypeCode()));

        EnvironnementEntity env = mapper.toEntity(dto);

        env.setId(null);
        env.setProjet(projet);
        env.setType(type);
        env.setActif(true);
        env.setCreatedAt(LocalDateTime.now());

        env = environnementRepository.save(env);

        return mapper.toDto(env);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public EnvironnementDTO update(Long id, EnvironnementDTO dto) {

        EnvironnementEntity env = getByIdOrThrow(id);

        mapper.updateEntity(env, dto); // partiel

        env.setUpdatedAt(LocalDateTime.now());
        environnementRepository.save(env);

        return mapper.toDto(env);
    }

    // =====================================================
    // DELETE LOGIQUE
    // =====================================================

    public void delete(Long id) {
        EnvironnementEntity entity = getByIdOrThrow(id);

        entity.setActif(false);
        entity.setUpdatedAt(LocalDateTime.now());

        environnementRepository.save(entity);
    }
}
