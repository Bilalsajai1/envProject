
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

    if (!this.session.isAuthenticated()) {
      return of(this.router.parseUrl('/auth/login'));
    }

    const requiredRoles = route.data['roles'] as string[] | undefined;

    return this.authContextService.ensureLoaded().pipe(
      map(ctx => {
        if (!ctx) {
          return this.router.parseUrl('/auth/login');
        }

        // aucune contrainte → accès OK
        if (!requiredRoles || requiredRoles.length === 0) {
          return true;
        }

        // rôles utilisateur → tableau → Set
        const rolesArray: string[] = ctx.user?.roles ?? [];
        const userRoles = new Set<string>(rolesArray);

        // si admin backend → on injecte "ADMIN" pour les gardes
        if (ctx.user?.admin) {
          userRoles.add('ADMIN');
        }

        const ok = requiredRoles.some(r => userRoles.has(r));

        if (!ok) {
          console.warn(
            'Accès refusé - Rôles requis:',
            requiredRoles,
            ' - Rôles utilisateur:',
            Array.from(userRoles)
          );
          return this.router.parseUrl('/auth/login'); // plus tard → vraie page 403
        }

        return true;
      })
    );
  }
}
