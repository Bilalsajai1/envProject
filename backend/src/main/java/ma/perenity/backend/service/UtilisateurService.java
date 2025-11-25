package ma.perenity.backend.service;

import ma.perenity.backend.dto.UserCreateUpdateDTO;
import ma.perenity.backend.dto.UserDTO;

import java.util.List;

public interface UtilisateurService {

    List<UserDTO> getAll();

    UserDTO getById(Long id);

    UserDTO create(UserCreateUpdateDTO dto);

    UserDTO update(Long id, UserCreateUpdateDTO dto);

    void delete(Long id);
}
