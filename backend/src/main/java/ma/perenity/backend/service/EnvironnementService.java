// src/main/java/ma/perenity/backend/service/EnvironnementService.java
package ma.perenity.backend.service;

import ma.perenity.backend.dto.EnvironnementDTO;
import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.entities.EnvironnementEntity;

import java.util.List;

public interface EnvironnementService {

    List<EnvironnementDTO> getEnvironmentsByProjetAndType(Long projetId, String typeCode);

    EnvironnementEntity getByIdOrThrow(Long id);

    EnvironnementDTO create(EnvironnementDTO dto);

    EnvironnementDTO update(Long id, EnvironnementDTO dto);

    void delete(Long id);

    PaginatedResponse<EnvironnementDTO> search(PaginationRequest req);
}
