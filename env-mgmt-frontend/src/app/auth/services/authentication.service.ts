import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { SessionStorageService, StoredUser } from './session-storage.service';
import { environment } from '../../config/environment';

export interface LoginPayload {
  username: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private readonly baseUrl = environment.apiUrl;
  currentUser$ = new BehaviorSubject<StoredUser | null>(null);

  constructor(
    private http: HttpClient,
    private session: SessionStorageService
  ) {
    const existing = this.session.userDetail;
    if (existing) {
      this.currentUser$.next(existing);
    }
  }

  // ⚠︎ Nécessite un POST /auth sur le backend
  login(payload: LoginPayload): Observable<StoredUser> {
    return this.http.post<StoredUser>(`${this.baseUrl}/auth`, payload).pipe(
      tap(user => {
        this.session.setUserDetail(user);
        this.currentUser$.next(user);
      })
    );
  }

  logout(): void {
    this.session.clear();
    this.currentUser$.next(null);
  }

  isAuthenticated(): boolean {
    return this.session.isAuthenticated();
  }

  hasRole(role: string): boolean {
    return this.session.roles.includes(role);
  }

  getCurrentUser(): StoredUser | null {
    return this.session.userDetail;
  }
}
