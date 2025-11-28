package ma.perenity.backend.service.impl;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProfilCreateUpdateDTO;
import ma.perenity.backend.dto.ProfilDTO;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.ProfilRoleEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.mapper.ProfilMapper;
import ma.perenity.backend.repository.ProfilRepository;
import ma.perenity.backend.repository.ProfilRoleRepository;
import ma.perenity.backend.repository.RoleRepository;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.ProfilService;
import ma.perenity.backend.specification.EntitySpecification;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfilServiceImpl implements ProfilService {

    private final ProfilRepository profilRepository;
    private final RoleRepository roleRepository;
    private final ProfilRoleRepository profilRoleRepository;
    private final ProfilMapper mapper;
    private final PermissionService permissionService;

    private void checkAdmin() {
        if (!permissionService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administration des profils réservée à l'administrateur");
        }
    }

    @Override
    public List<ProfilDTO> getAll() {
        checkAdmin();
        return mapper.toDtoList(profilRepository.findByActifTrue());
    }

    @Override
    public ProfilDTO getById(Long id) {
        checkAdmin();
        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));
        return mapper.toDto(profil);
    }

    @Override
    public ProfilDTO create(ProfilCreateUpdateDTO dto) {
        checkAdmin();

        ProfilEntity profil = ProfilEntity.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .description(dto.getDescription())
                .admin(dto.getAdmin() != null ? dto.getAdmin() : false)
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .build();

        return mapper.toDto(profilRepository.save(profil));
    }

    @Override
    public ProfilDTO update(Long id, ProfilCreateUpdateDTO dto) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        profil.setCode(dto.getCode());
        profil.setLibelle(dto.getLibelle());
        profil.setDescription(dto.getDescription());

        if (dto.getAdmin() != null) {
            profil.setAdmin(dto.getAdmin());
        }
        if (dto.getActif() != null) {
            profil.setActif(dto.getActif());
        }

        return mapper.toDto(profilRepository.save(profil));
    }

    @Override
    public void delete(Long id) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        profil.setActif(false);
        profilRepository.save(profil);
    }

    @Override
    public List<Long> getRoleIds(Long profilId) {
        checkAdmin();
        return profilRoleRepository.findRoleIdsByProfilId(profilId);
    }

    @Override
    public void assignRoles(Long profilId, List<Long> roleIds) {
        checkAdmin();

        ProfilEntity profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil introuvable"));

        profilRoleRepository.deleteByProfilId(profilId);

        for (Long roleId : roleIds) {
            RoleEntity role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role introuvable : " + roleId));

            ProfilRoleEntity pr = new ProfilRoleEntity();
            pr.setProfil(profil);
            pr.setRole(role);
            profilRoleRepository.save(pr);
        }
    }

    @Override
    public PaginatedResponse<ProfilDTO> search(PaginationRequest req) {
        checkAdmin();

        Sort sort = req.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortField()).descending()
                : Sort.by(req.getSortField()).ascending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        EntitySpecification<ProfilEntity> specBuilder = new EntitySpecification<>();

        Page<ProfilEntity> page = profilRepository.findAll(
                specBuilder.getSpecification(req.getFilters()),
                pageable
        );

        return PaginatedResponse.fromPage(
                page.map(mapper::toDto)
        );
    }
}
