package ma.perenity.backend.service;

import ma.perenity.backend.dto.MenuDTO;
import ma.perenity.backend.dto.MenuCreateUpdateDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;

import java.util.List;

public interface MenuService {
    List<MenuDTO> findAll();
    MenuDTO findById(Long id);
    MenuDTO create(MenuCreateUpdateDTO dto);
    MenuDTO update(Long id, MenuCreateUpdateDTO dto);
    void delete(Long id);
    List<MenuDTO> findByEnvironmentTypeCode(String code);

    PaginatedResponse<MenuDTO> search(PaginationRequest req);


}
