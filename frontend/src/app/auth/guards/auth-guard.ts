// src/app/auth/guards/auth.guard.ts

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

    // 1️⃣ Vérifier le token
    if (!this.session.isAuthenticated()) {
      console.warn('⚠️ Non authentifié, redirection vers /auth/login');
      return of(this.router.parseUrl('/auth/login'));
    }

    const requiredRoles = route.data['roles'] as string[] | undefined;

    // 2️⃣ Charger le contexte si nécessaire
    return this.authContextService.ensureLoaded().pipe(
      map(ctx => {
        if (!ctx) {
          console.warn('⚠️ Contexte non chargé, redirection vers /auth/login');
          return this.router.parseUrl('/auth/login');
        }

        // 3️⃣ Aucune contrainte de rôle → accès OK
        if (!requiredRoles || requiredRoles.length === 0) {
          return true;
        }

        // 4️⃣ Construire l'ensemble des rôles utilisateur
        const rolesArray: string[] = ctx.user?.roles ?? [];
        const userRoles = new Set<string>(rolesArray);

        // 5️⃣ Admin → ajouter le rôle "ADMIN" pour les guards
        if (ctx.user?.admin) {
          userRoles.add('ADMIN');
        }

        // 6️⃣ Vérifier si l'utilisateur a au moins un des rôles requis
        const hasAccess = requiredRoles.some(r => userRoles.has(r));

        if (!hasAccess) {
          console.warn(
            '⚠️ Accès refusé',
            '- Rôles requis:', requiredRoles,
            '- Rôles utilisateur:', Array.from(userRoles)
          );
          return this.router.parseUrl('/auth/access-denied');
        }

        console.log('✅ Accès autorisé pour', state.url);
        return true;
      })
    );
  }
}
