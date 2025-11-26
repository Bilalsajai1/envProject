import { Injectable } from '@angular/core';
import { BehaviorSubject, tap } from 'rxjs';
import { AuthContext } from '../environement/models/auth-context.model';
import { HttpClient } from '@angular/common/http';
import {environment} from '../../configuration/environement.config';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private apiUrl = environment.apiUrl;
  auth$ = new BehaviorSubject<AuthContext | null>(null);

  constructor(private http: HttpClient) {}

  /** Charge /auth/me APRES que le token soit déjà stocké */
  init() {
    return this.http.get<AuthContext>(`${this.apiUrl}/auth/me`).pipe(
      tap(ctx => {
        this.auth$.next(ctx);
        localStorage.setItem('auth_ctx', JSON.stringify(ctx));
      })
    );
  }

  /** Retourne le token Keycloak */
  getToken(): string | null {
    return localStorage.getItem('kc_token');
  }

  /** Sauvegarde du token Keycloak après le callback */
  saveToken(token: string): void {
    localStorage.setItem('kc_token', token);
  }

  /** Vérifie si un utilisateur est connecté */
  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  /** Déconnexion */
  redirectToLogin(): void {
    const keycloakUrl =
      'http://localhost:8080/realms/env-mgmt/protocol/openid-connect/auth' +
      '?client_id=angular-client' +
      '&redirect_uri=' + encodeURIComponent('http://localhost:4200/auth/callback') +
      '&response_type=code';

    window.location.href = keycloakUrl;
  }
}
