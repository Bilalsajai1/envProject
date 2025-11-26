package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProjetDTO;
import ma.perenity.backend.entities.ProjetEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.repository.ProjetRepository;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;


    public List<ProjetDTO> getProjectsByEnvironmentType(String typeCode) {
        return projetRepository.findByEnvironmentTypeCode(typeCode)
                .stream()
                .map(this::toDto)
                .toList();
    }


    public List<ProjetDTO> getAll() {
        return projetRepository.findByActifTrue()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ProjetDTO getById(Long id) {
        ProjetEntity entity = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec id = " + id));
        return toDto(entity);
    }

    public ProjetDTO create(ProjetDTO dto) {
        ProjetEntity entity = new ProjetEntity();
        entity.setCode(dto.getCode());
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        entity.setActif(dto.getActif() != null ? dto.getActif() : Boolean.TRUE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        entity = projetRepository.save(entity);
        return toDto(entity);
    }

    public ProjetDTO update(Long id, ProjetDTO dto) {
        ProjetEntity entity = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable avec id = " + id));

        entity.setCode(dto.getCode());
        entity.setLibelle(dto.getLibelle());
        entity.setDescription(dto.getDescription());
        if (dto.getActif() != null) {
            entity.setActif(dto.getActif());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        entity = projetRepository.save(entity);
        return toDto(entity);
    }


    public void delete(Long id) {
        ProjetEntity projet = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet introuvable"));

        projet.setActif(false);
        projet.setUpdatedAt(LocalDateTime.now());

        projetRepository.save(projet);
    }


    private ProjetDTO toDto(ProjetEntity p) {
        return ProjetDTO.builder()
                .id(p.getId())
                .code(p.getCode())
                .libelle(p.getLibelle())
                .description(p.getDescription())
                .actif(p.getActif())
                .build();
    }
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
                page.map(this::toDto)
        );
    }
}
