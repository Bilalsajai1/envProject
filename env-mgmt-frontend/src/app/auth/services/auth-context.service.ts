// src/app/auth/services/auth-context.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, map, catchError } from 'rxjs/operators';
import { AuthContext } from '../models/auth-context.model';
import { environment } from '../../config/environment';

@Injectable({ providedIn: 'root' })
export class AuthContextService {

  private readonly apiUrl = `${environment.apiUrl}/auth/me`;
  private context$ = new BehaviorSubject<AuthContext | null>(null);

  constructor(private http: HttpClient) {}

  loadContext(): Observable<AuthContext> {
    return this.http.get<any>(this.apiUrl).pipe(
      map(response => {
        const rawRoles = response?.user?.roles ?? [];
        const rolesArray: string[] = Array.isArray(rawRoles)
          ? rawRoles
          : Object.values(rawRoles ?? {}) as string[];

        return {
          ...response,
          user: {
            ...response.user,
            roles: rolesArray
          }
        } as AuthContext;
      }),
      tap(ctx => this.context$.next(ctx))
    );
  }

  loadAuthContext(): Observable<AuthContext> {
    return this.loadContext();
  }

  ensureLoaded(): Observable<AuthContext | null> {
    const current = this.context$.value;
    if (current) {
      return of(current);
    }

    return this.loadContext().pipe(
      map(ctx => ctx),
      catchError(err => {
        console.error('Erreur chargement /auth/me', err);
        this.context$.next(null);
        return of(null);
      })
    );
  }

  getContext$(): Observable<AuthContext | null> {
    return this.context$.asObservable();
  }

  getCurrentContext(): AuthContext | null {
    return this.context$.value;
  }
}
