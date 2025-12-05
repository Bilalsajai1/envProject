package ma.perenity.backend.service;

import ma.perenity.backend.dto.EnvApplicationDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;

import java.util.List;

public interface EnvApplicationService {

    List<EnvApplicationDTO> getByEnvironnement(Long envId, String search);

    EnvApplicationDTO create(EnvApplicationDTO dto);

    EnvApplicationDTO update(Long id, EnvApplicationDTO dto);

    void delete(Long id);

    PaginatedResponse<EnvApplicationDTO> search(PaginationRequest req);
}
