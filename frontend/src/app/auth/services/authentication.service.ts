// src/app/auth/services/authentication.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { environment } from '../../config/environment';
import { SessionStorageService, StoredUser } from './session-storage.service';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;   // "Bearer"
  expiresIn?: number;
  username: string;
  roles?: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthenticationService {

  private readonly loginUrl = `${environment.apiUrl}/auth`;
  private readonly changePasswordUrl = `${environment.apiUrl}/auth/change-password`;

  constructor(
    private http: HttpClient,
    private session: SessionStorageService
  ) {}

  /**
   * Appelle /auth/login et stocke le token dans localStorage
   */
  login(req: LoginRequest): Observable<void> {
    return this.http.post<LoginResponse>(this.loginUrl, req).pipe(
      tap(res => {
        const expiresAt = res.expiresIn
          ? Date.now() + res.expiresIn * 1000
          : undefined;
        const stored: StoredUser = {
          accessToken: res.accessToken,
          refreshToken: res.refreshToken,
          tokenType: res.tokenType,
          expiresIn: res.expiresIn,
          expiresAt,
          username: res.username,
          roles: res.roles ?? []
        };
        this.session.setUserDetail(stored);
      }),
      map(() => void 0)
    );
  }

  /**
   * Déconnexion : on nettoie le storage
   */
  logout(): void {
    this.session.clear();
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>(this.changePasswordUrl, {
      currentPassword,
      newPassword
    });
  }
}
