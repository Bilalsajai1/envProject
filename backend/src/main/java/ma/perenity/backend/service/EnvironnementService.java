package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.EnvironmentTypeEntity;
import ma.perenity.backend.entities.EnvironnementEntity;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.EnvironnementMapper;
import ma.perenity.backend.repository.EnvironmentTypeRepository;
import ma.perenity.backend.repository.EnvironnementRepository;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

        return environnementRepository
                .findByProjet_IdAndType_CodeAndActifTrue(projetId, typeCode)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public EnvironnementEntity getByIdOrThrow(Long id) {
        return environnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Environnement not found with id = " + id
                ));
    }

    // =====================================================
    // CREATE
    // =====================================================

    public EnvironnementDTO create(EnvironnementDTO dto) {

        ProjetEntity projet = projetRepository.findById(dto.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet not found with id = " + dto.getProjetId()
                ));

        EnvironmentTypeEntity type = typeRepository.findByCode(dto.getTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EnvironmentType not found with code = " + dto.getTypeCode()
                ));

        EnvironnementEntity env = mapper.toEntity(dto);

        env.setId(null);
        env.setProjet(projet);
        env.setType(type);
        env.setActif(true); // par défaut actif à la création

        env = environnementRepository.save(env);

        return mapper.toDto(env);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public EnvironnementDTO update(Long id, EnvironnementDTO dto) {

        EnvironnementEntity env = getByIdOrThrow(id);

        // update partiel via mapper (NullValuePropertyMappingStrategy.IGNORE)
        mapper.updateEntityFromDto(dto, env);

        env = environnementRepository.save(env);

        return mapper.toDto(env);
    }

    // =====================================================
    // DELETE LOGIQUE
    // =====================================================

    public void delete(Long id) {
        EnvironnementEntity entity = getByIdOrThrow(id);

        entity.setActif(false);

        environnementRepository.save(entity);
    }

    // =====================================================
    // SEARCH (pagination + filtres dynamiques)
    // =====================================================

    public PaginatedResponse<EnvironnementDTO> search(PaginationRequest req) {

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<EnvironnementEntity> specBuilder = new EntitySpecification<>();

        Page<EnvironnementEntity> page = environnementRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
