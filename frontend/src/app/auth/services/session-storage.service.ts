import { Injectable } from '@angular/core';

const USER_DETAILS_KEY = 'envmgmt_user';

export interface StoredUser {
  accessToken: string;
  refreshToken?: string;
  tokenType?: string;  // ex: "Bearer"
  expiresIn?: number;
  expiresAt?: number;  // timestamp in ms
  username: string;
  roles: string[];     // can stay empty if fetched via /auth/me
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
    const u = this.userDetail;
    if (!u?.accessToken) return false;
    if (u.expiresAt && Date.now() > u.expiresAt) {
      return false;
    }
    return true;
  }

  get roles(): string[] {
    return this.userDetail?.roles ?? [];
  }

  getAccessToken(): string | null {
    if (!this.isAuthenticated()) return null;
    return this.userDetail?.accessToken ?? null;
  }
}
