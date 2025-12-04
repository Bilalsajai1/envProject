// src/main/java/ma/perenity/backend/service/EnvironmentTypeService.java
package ma.perenity.backend.service;

import ma.perenity.backend.dto.EnvironmentTypeDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;

import java.util.List;

public interface EnvironmentTypeService {

    List<EnvironmentTypeDTO> getAll();

    List<EnvironmentTypeDTO> getAllActive();

    EnvironmentTypeDTO getById(Long id);

    EnvironmentTypeDTO create(EnvironmentTypeDTO dto);

    EnvironmentTypeDTO update(Long id, EnvironmentTypeDTO dto);

    void delete(Long id);

    PaginatedResponse<EnvironmentTypeDTO> search(PaginationRequest req);
}
