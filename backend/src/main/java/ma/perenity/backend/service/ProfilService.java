package ma.perenity.backend.service;

import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProfilCreateUpdateDTO;
import ma.perenity.backend.dto.ProfilDTO;

import java.util.List;

public interface ProfilService {

    List<ProfilDTO> getAll();

    ProfilDTO getById(Long id);

    ProfilDTO create(ProfilCreateUpdateDTO dto);

    ProfilDTO update(Long id, ProfilCreateUpdateDTO dto);

    void delete(Long id);

    List<Long> getRoleIds(Long profilId);

    void assignRoles(Long profilId, List<Long> roleIds);
    PaginatedResponse<ProfilDTO> search(PaginationRequest req);

}
