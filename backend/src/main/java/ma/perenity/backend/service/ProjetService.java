// src/main/java/ma/perenity/backend/service/ProjetService.java
package ma.perenity.backend.service;

import ma.perenity.backend.dto.PaginatedResponse;
import ma.perenity.backend.dto.PaginationRequest;
import ma.perenity.backend.dto.ProjetDTO;

import java.util.List;

public interface ProjetService {

    List<ProjetDTO> getProjectsByEnvironmentType(String typeCode);

    List<ProjetDTO> getAll();

    ProjetDTO getById(Long id);

    ProjetDTO create(ProjetDTO dto);

    ProjetDTO update(Long id, ProjetDTO dto);

    void delete(Long id);

    PaginatedResponse<ProjetDTO> search(PaginationRequest req);
}
