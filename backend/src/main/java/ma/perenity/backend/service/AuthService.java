package ma.perenity.backend.service;

import lombok.RequiredArgsConstructor;
import ma.perenity.backend.dto.AuthContextDTO;
import ma.perenity.backend.dto.MenuDTO;
import ma.perenity.backend.entities.MenuEntity;
import ma.perenity.backend.entities.ProfilEntity;
import ma.perenity.backend.entities.RoleEntity;
import ma.perenity.backend.entities.UtilisateurEntity;
import ma.perenity.backend.excepion.ResourceNotFoundException;
import ma.perenity.backend.mapper.MenuMapper;
import ma.perenity.backend.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final ProfilRoleRepository profilRoleRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;  // 🔥 AJOUT

    public AuthContextDTO getCurrentUserContext() {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = jwt.getClaimAsString("email");

        UtilisateurEntity user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        ProfilEntity profil = user.getProfil();

        List<RoleEntity> roles = profilRoleRepository.findRolesByProfil(profil.getId());

        List<MenuEntity> menus = menuRepository.findMenusByRoles(
                roles.stream().map(RoleEntity::getId).toList()
        );

        return AuthContextDTO.builder()
                .userId(user.getId())
                .username(user.getLastName() + " " + user.getFirstName())
                .email(user.getEmail())
                .profilCode(profil.getCode())
                .profilLibelle(profil.getLibelle())
                .roles(roles.stream().map(RoleEntity::getCode).toList())
                .menus(
                        menus.stream().map(menuMapper::toDTO).toList()   // 🔥 NOUVEAU MAPPING
                )
                .build();
    }
}
