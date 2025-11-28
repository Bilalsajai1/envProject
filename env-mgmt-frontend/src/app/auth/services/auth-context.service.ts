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

  /** Chargement explicite du contexte */
  loadContext(): Observable<AuthContext> {
    return this.http.get<any>(this.apiUrl).pipe(
      map(response => {
        // ✅ Convertir roles array en Set
        return {
          ...response,
          user: {
            ...response.user,
            roles: new Set<string>(response.user.roles || [])
          }
        } as AuthContext;
      }),
      tap(ctx => this.context$.next(ctx))
    );
  }

  /** Alias pour compatibilité */
  loadAuthContext(): Observable<AuthContext> {
    return this.loadContext();
  }

  /** Utilisé par le Guard */
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

  /** Observable pour les composants */
  getContext$(): Observable<AuthContext | null> {
    return this.context$.asObservable();
  }

  /** Accès synchrone */
  getCurrentContext(): AuthContext | null {
    return this.context$.value;
  }
}
