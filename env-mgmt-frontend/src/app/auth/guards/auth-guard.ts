// src/app/auth/guards/auth-guard.ts

import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthContextService } from '../services/auth-context.service';
import { SessionStorageService } from '../services/session-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private session: SessionStorageService,
    private authContextService: AuthContextService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {

    // 1️⃣ Pas de token → on redirige vers login
    if (!this.session.isAuthenticated()) {
      return of(this.router.parseUrl('/auth/login'));
    }

    const requiredRoles = route.data['roles'] as string[] | undefined;

    // 2️⃣ On s'assure que /auth/me est chargé
    return this.authContextService.ensureLoaded().pipe(
      map(ctx => {
        if (!ctx) {
          return this.router.parseUrl('/auth/login');
        }

        // 3️⃣ Check rôles si la route en demande
        if (requiredRoles && requiredRoles.length > 0) {
          const userRoles = ctx.user?.roles ?? new Set<string>();

          // ✅ CORRECTION : Utiliser .has() au lieu de .includes()
          const ok = requiredRoles.some(r => userRoles.has(r));

          if (!ok) {
            console.warn('Accès refusé - Rôles requis:', requiredRoles);
            // TODO: créer une vraie page 403
            return this.router.parseUrl('/auth/login');
          }
        }

        return true;
      })
    );
  }
}
