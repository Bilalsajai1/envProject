package ma.perenity.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.*;
import ma.perenity.backend.service.AuthContextService;
import ma.perenity.backend.service.LoginService;
import ma.perenity.backend.service.PermissionService;
import ma.perenity.backend.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;

    private final AuthContextService authContextService;
    private final PermissionService permissionService;
    private final UtilisateurService utilisateurService;


    @GetMapping("/me")
    public ResponseEntity<AuthContextDTO> me() {
        return ResponseEntity.ok(authContextService.getCurrentContext());
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/permissions")
    public ResponseEntity<UserPermissionsDTO> getCurrentUserPermissions() {
        return ResponseEntity.ok(permissionService.getCurrentUserPermissions());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changeOwnPassword(@RequestBody @Valid ChangePasswordRequest request) {
        utilisateurService.changeOwnPassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
