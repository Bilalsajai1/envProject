package ma.perenity.backend.controller;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.AuthContextDTO;
import ma.perenity.backend.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/auth/me")
    public AuthContextDTO getAuthContext() {
        return authService.getCurrentUserContext();
    }
}