package ma.perenity.backend.service;

import ma.perenity.backend.dto.LoginRequest;
import ma.perenity.backend.dto.LoginResponse;

public interface LoginService {

    LoginResponse login(LoginRequest request);
}
