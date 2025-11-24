import { Injectable } from '@angular/core';
import {environment} from '../../configuration/environement.config';
import {BehaviorSubject, tap} from 'rxjs';
import {AuthContext} from '../environement/models/auth-context.model';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = environment.apiUrl;

  auth$ = new BehaviorSubject<AuthContext | null>(null);

  constructor(private http: HttpClient) {}

  /** Charge /auth/me au démarrage */
  init() {
    return this.http.get<AuthContext>(`${this.apiUrl}/auth/me`).pipe(
      tap(ctx => {
        this.auth$.next(ctx);
        localStorage.setItem('auth_ctx', JSON.stringify(ctx));
      })
    );
  }

  /** Retourne true si utilisateur chargé */
  isLoggedIn(): boolean {
    return this.auth$.value !== null;
  }

  /** Récupérer le token depuis Keycloak Storage */
  getToken(): string | null {
    return localStorage.getItem('kc_token');
  }

  /** Déconnexion : redirect vers Keycloak */
  logout(): void {
    localStorage.clear();

    window.location.href =
      'http://localhost:8080/realms/env-mgmt/protocol/openid-connect/logout' +
      '?redirect_uri=http://localhost:4200/';
  }
}
