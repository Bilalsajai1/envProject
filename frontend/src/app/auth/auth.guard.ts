import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import {AuthService} from './auth-service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private auth: AuthService,

  ) {}

  canActivate(): boolean {

    // Si déjà authentifié → OK
    if (this.auth.isLoggedIn()) {
      return true;
    }

    // Sinon → redirection vers Keycloak
    const keycloakUrl =
      'http://localhost:8080/realms/env-mgmt/protocol/openid-connect/auth' +
      '?client_id=angular-client' +
      '&redirect_uri=http://localhost:4200/auth/callback' +
      '&response_type=code';

    window.location.href = keycloakUrl;
    return false;
  }
}
