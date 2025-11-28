package ma.perenity.backend.service.impl;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.UserCreateUpdateDTO;
import ma.perenity.backend.dto.UserDTO;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.UtilisateurEntity;
import ma.perenity.backend.mapper.UserMapper;
import ma.perenity.backend.repository.ProfilRepository;
import ma.perenity.backend.repository.UtilisateurRepository;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.UtilisateurService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UserMapper userMapper;
    private final PermissionService permissionService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administration des utilisateurs réservée à l'administrateur");
        }
    }

    @Override
    public List<UserDTO> getAll() {
        checkAdmin();
        return userMapper.toDtoList(utilisateurRepository.findByActifTrue());
    }

    @Override
    public UserDTO getById(Long id) {
        checkAdmin();
        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return userMapper.toDto(entity);
    }

    @Override
    public UserDTO create(UserCreateUpdateDTO dto) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(dto.getProfilId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profil introuvable"));

        UtilisateurEntity entity = UtilisateurEntity.builder()
                .code(dto.getCode())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .profil(profil)
                .build();

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public UserDTO update(Long id, UserCreateUpdateDTO dto) {
        checkAdmin();

        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        entity.setCode(dto.getCode());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());

        if (dto.getActif() != null) {
            entity.setActif(dto.getActif());
        }

        if (dto.getProfilId() != null) {
            ProfilEntity profil = profilRepository.findById(dto.getProfilId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profil introuvable"));
            entity.setProfil(profil);
        }

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        checkAdmin();

        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        entity.setActif(false);
        utilisateurRepository.save(entity);
    }

    @Override
    public PaginatedResponse<UserDTO> search(PaginationRequest req) {
        checkAdmin();

        Sort sort = req.getSortDirection().equalsIgnoreCase("asc")
                ? Sort.by(req.getSortField()).ascending()
                : Sort.by(req.getSortField()).descending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Map<String, Object> rawFilters = req.getFilters() != null
                ? new HashMap<>(req.getFilters())
                : new HashMap<>();

        String search = null;
        Object searchObj = rawFilters.remove("search");
        if (searchObj != null) {
            search = searchObj.toString().trim();
            if (search.isEmpty()) {
                search = null;
            }
        }

        EntitySpecification<UtilisateurEntity> specBuilder = new EntitySpecification<>();
        Specification<UtilisateurEntity> spec = specBuilder.getSpecification(rawFilters);

        if (search != null) {
            final String term = "%" + search.toLowerCase() + "%";

            Specification<UtilisateurEntity> globalSearchSpec = (root, query, cb) -> {
                Join<Object, Object> profilJoin = root.join("profil", JoinType.LEFT);

                return cb.or(
                        cb.like(cb.lower(root.get("code")), term),
                        cb.like(cb.lower(root.get("firstName")), term),
                        cb.like(cb.lower(root.get("lastName")), term),
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(profilJoin.get("libelle")), term)
                );
            };

            spec = spec.and(globalSearchSpec);
        }

        Page<UtilisateurEntity> page = utilisateurRepository.findAll(spec, pageable);

        return PaginatedResponse.fromPage(
                page.map(userMapper::toDto)
        );
    }
}
