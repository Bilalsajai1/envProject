package ma.perenity.backend.service.impl;

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
import ma.perenity.backend.service.UtilisateurService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getAll() {
        return userMapper.toDtoList(utilisateurRepository.findAll());
    }

    @Override
    public UserDTO getById(Long id) {
        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return userMapper.toDto(entity);
    }

    @Override
    public UserDTO create(UserCreateUpdateDTO dto) {

        ProfilEntity profil = profilRepository.findById(dto.getProfilId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profil introuvable"));

        UtilisateurEntity entity = UtilisateurEntity.builder()
                .code(dto.getCode())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .profil(profil)
                .build();

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public UserDTO update(Long id, UserCreateUpdateDTO dto) {

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

        entity.setUpdatedAt(LocalDateTime.now());

        return userMapper.toDto(utilisateurRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        UtilisateurEntity entity = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        utilisateurRepository.delete(entity);
    }
    @Override
    public PaginatedResponse<UserDTO> search(PaginationRequest req) {

        Sort sort = req.getSortDirection().equalsIgnoreCase("asc")
                ? Sort.by(req.getSortField()).ascending()
                : Sort.by(req.getSortField()).descending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<UtilisateurEntity> filter = new EntitySpecification<>();

        Page<UtilisateurEntity> page = utilisateurRepository.findAll(
                filter.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(userMapper::toDto)
        );
    }

}
