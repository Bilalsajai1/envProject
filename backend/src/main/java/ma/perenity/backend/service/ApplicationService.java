package ma.perenity.backend.service;


import ma.perenity.backend.dto.ApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;

import java.util.List;

public interface ApplicationService {

    List<ApplicationDTO> getAll();

    List<ApplicationDTO> getAllActive();

    ApplicationDTO getById(Long id);

    ApplicationDTO create(ApplicationDTO dto);

    ApplicationDTO update(Long id, ApplicationDTO dto);

    void delete(Long id);

    PaginatedResponse<ApplicationDTO> search(PaginationRequest req);
}