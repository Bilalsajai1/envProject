package ma.perenity.backend.controller;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.AuthContextDTO;
import ma.perenity.backend.service.AuthContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthContextController {

    private final AuthContextService authContextService;

    @GetMapping("/me")
    public ResponseEntity<AuthContextDTO> me() {
        return ResponseEntity.ok(authContextService.getCurrentContext());
    }
}
