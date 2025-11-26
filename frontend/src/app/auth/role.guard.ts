import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from './auth-service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const authContext = this.auth.auth$.value;

    if (!authContext) {
      this.router.navigate(['/']);
      return false;
    }

    // Si admin = true, accès à TOUT
    if (authContext.admin) {
      return true;
    }

    // Sinon vérifier les rôles spécifiques
    const requiredRoles = route.data['roles'] as string[];

    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    const hasRequiredRole = requiredRoles.some(role =>
      authContext.roles.includes(role)
    );

    if (!hasRequiredRole) {
      this.router.navigate(['/dashboard']);
      return false;
    }

    return true;
  }
}
