package ma.perenity.backend.service;

import ma.perenity.backend.dto.AuthContextDTO;

public interface AuthContextService {

    AuthContextDTO getCurrentContext();
}
