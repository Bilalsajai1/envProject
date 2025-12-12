package ma.perenity.backend.service;

import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.RoleCreateUpdateDTO;
import ma.perenity.backend.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    List<RoleDTO> getAll();

    RoleDTO getById(Long id);

    RoleDTO create(RoleCreateUpdateDTO dto);

    RoleDTO update(Long id, RoleCreateUpdateDTO dto);

    void delete(Long id);

    List<RoleDTO> getByEnvironnement(Long envId);

    PaginatedResponse<RoleDTO> search(PaginationRequest req);

}
