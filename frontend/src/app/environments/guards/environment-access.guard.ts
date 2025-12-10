import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  RouterStateSnapshot,
  UrlTree,
  Router
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthContextService } from '../../auth/services/auth-context.service';

@Injectable({ providedIn: 'root' })
export class EnvironmentAccessGuard implements CanActivate {

  constructor(
    private readonly authContext: AuthContextService,
    private readonly router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> {
    const typeCode = (route.paramMap.get('typeCode') || '').toUpperCase();
    if (!typeCode) {
      return of(this.router.parseUrl('/auth/access-denied'));
    }

    return this.authContext.ensureLoaded().pipe(
      map(ctx => {
        if (!ctx) return this.router.parseUrl('/auth/login');

        const canView = this.authContext.canViewType(typeCode);
        return canView ? true : this.router.parseUrl('/auth/access-denied');
      })
    );
  }
}
