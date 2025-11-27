package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.ProjetMapper;
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
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final ProjetMapper projetMapper;

    // ============================================================
    // GET par type d'environnement (actifs + inactifs)
    // ============================================================

    public List<ProjetDTO> getProjectsByEnvironmentType(String typeCode) {
        return projetRepository.findByEnvironmentTypeCode(typeCode)
                .stream()
                .map(projetMapper::toDto)
                .toList();
    }

    // ============================================================
    // GET ALL (uniquement actifs)
    // ============================================================

    public List<ProjetDTO> getAll() {
        return projetRepository.findByActifTrue()
                .stream()
                .map(projetMapper::toDto)
                .toList();
    }

    // ============================================================
    // GET BY ID (uniquement actif)
    // ============================================================

    public ProjetDTO getById(Long id) {
        ProjetEntity entity = projetRepository.findById(id)
                .filter(ProjetEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet introuvable (ou inactif) avec id = " + id
                ));

        return projetMapper.toDto(entity);
    }

    // ============================================================
    // CREATE
    // ============================================================

    public ProjetDTO create(ProjetDTO dto) {

        ProjetEntity entity = projetMapper.toEntity(dto);
        entity.setId(null);

        // si non renseigné, on force actif = true
        if (entity.getActif() == null) {
            entity.setActif(true);
        }

        entity = projetRepository.save(entity);
        return projetMapper.toDto(entity);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public ProjetDTO update(Long id, ProjetDTO dto) {
        ProjetEntity entity = projetRepository.findById(id)
                .filter(ProjetEntity::getActif)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Projet introuvable (ou inactif) avec id = " + id
                ));

        // update partiel via mapper (ignore les nulls)
        projetMapper.updateEntityFromDto(dto, entity);

        entity = projetRepository.save(entity);
        return projetMapper.toDto(entity);
    }

    // ============================================================
    // DELETE LOGIQUE
    // ============================================================

    public void delete(Long id) {
        ProjetEntity projet = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec id = " + id));

        projet.setActif(false);

        projetRepository.save(projet);
    }

    // ============================================================
    // SEARCH (pagination + filtres dynamiques)
    // ============================================================

    public PaginatedResponse<ProjetDTO> search(PaginationRequest req) {

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<ProjetEntity> specBuilder = new EntitySpecification<>();

        Page<ProjetEntity> page = projetRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(projetMapper::toDto)
        );
    }
}
