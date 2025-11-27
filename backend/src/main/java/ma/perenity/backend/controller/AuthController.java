package ma.perenity.backend.controller;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.AuthContextDTO;
import ma.perenity.backend.dto.LoginRequest;
import ma.perenity.backend.dto.LoginResponse;
import ma.perenity.backend.service.AuthService;
import ma.perenity.backend.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginService loginService;

    // 🔐 Contexte utilisateur (JWT obligatoire)
    @GetMapping("/auth/me")
    public ResponseEntity<AuthContextDTO> getAuthContext() {
        return ResponseEntity.ok(authService.getCurrentUserContext());
    }

    // 🧨 Login (public, sans JWT)
    @PostMapping("/auth")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }
}
