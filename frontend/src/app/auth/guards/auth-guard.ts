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

        if (!requiredRoles || requiredRoles.length === 0) {
          return true;
        }

        const rolesArray: string[] = ctx.user?.roles ?? [];
        const userRoles = new Set<string>(rolesArray);

        if (ctx.user?.admin) {
          userRoles.add('ADMIN');
        }

        const hasAccess = requiredRoles.some(r => userRoles.has(r));

        if (!hasAccess) {
          return this.router.parseUrl('/auth/access-denied');
        }
        return true;
      })
    );
  }
}
