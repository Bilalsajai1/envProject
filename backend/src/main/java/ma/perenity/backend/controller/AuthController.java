package ma.perenity.backend.controller;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.AuthContextDTO;
import ma.perenity.backend.dto.LoginRequest;
import ma.perenity.backend.dto.LoginResponse;
import ma.perenity.backend.dto.UserPermissionsDTO;
import ma.perenity.backend.service.AuthContextService;
import ma.perenity.backend.service.LoginService;
import ma.perenity.backend.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;

    private final AuthContextService authContextService;
    private final PermissionService permissionService;


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

}