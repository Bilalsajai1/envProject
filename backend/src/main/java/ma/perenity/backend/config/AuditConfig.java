package ma.perenity.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class AuditConfig {

    /**
     * Fournit le username depuis le JWT pour les champs @CreatedBy et @LastModifiedBy
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            try {
                var authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    // Récupérer le preferred_username du JWT
                    String username = jwt.getClaimAsString("preferred_username");
                    return Optional.ofNullable(username);
                }
            } catch (Exception e) {
                // En cas d'erreur (ex: pas authentifié), retourner "SYSTEM"
            }

            return Optional.of("SYSTEM");
        };
    }
}