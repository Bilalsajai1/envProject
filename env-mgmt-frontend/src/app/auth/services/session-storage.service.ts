import { Injectable } from '@angular/core';

const USER_DETAILS_KEY = 'envmgmt_user';

export interface StoredUser {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;  // ex: "Bearer"
  expiresIn?: number;
  username: string;
  roles: string[];     // tu peux les laisser [] si tu préfères les récupérer via /auth/me
}

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {

  get userDetail(): StoredUser | null {
    const raw = localStorage.getItem(USER_DETAILS_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as StoredUser;
    } catch {
      localStorage.removeItem(USER_DETAILS_KEY);
      return null;
    }
  }

  setUserDetail(user: StoredUser): void {
    localStorage.setItem(USER_DETAILS_KEY, JSON.stringify(user));
  }

  clear(): void {
    localStorage.removeItem(USER_DETAILS_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.userDetail?.accessToken;
  }

  get roles(): string[] {
    return this.userDetail?.roles ?? [];
  }

  getAccessToken(): string | null {
    return this.userDetail?.accessToken ?? null;
  }
}
