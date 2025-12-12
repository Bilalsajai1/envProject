package ma.perenity.backend.utilities;

import ma.perenity.backend.service.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class AdminGuard {

    private AdminGuard() {
    }

    public static void requireAdmin(PermissionService permissionService, String message) {
        if (permissionService == null) {
            throw new IllegalArgumentException("permissionService is required");
        }
        if (!permissionService.isAdmin()) {
            String errorMessage = (message != null && !message.isBlank())
                    ? message
                    : "Action reservee a l'administrateur";
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
        }
    }
}
