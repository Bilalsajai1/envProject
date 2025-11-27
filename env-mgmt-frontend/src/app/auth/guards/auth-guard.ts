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
import { SessionStorageService } from '../services/session-storage.service';
import { AuthContextService } from '../services/auth-context.service';

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

        if (requiredRoles && requiredRoles.length > 0) {
          const ok = requiredRoles.some(r => ctx.roles.includes(r));
          if (!ok) {
            // TODO: page 403 plus tard
            return this.router.parseUrl('/auth/login');
          }
        }

        return true;
      })
    );
  }
}
