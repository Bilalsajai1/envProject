import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import {AuthService} from './auth-service';


@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): boolean {

    if (this.auth.isLoggedIn()) {
      return true;
    }

    const kcUrl =
      'http://localhost:8080/realms/env-mgmt/protocol/openid-connect/auth' +
      '?client_id=angular-client' +
      '&redirect_uri=http://localhost:4200/' +
      '&response_type=code';

    window.location.href = kcUrl;

    return false;
  }
}
