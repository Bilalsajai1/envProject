import { Component } from '@angular/core';
import {Router} from '@angular/router';

@Component({
  selector: 'app-main-layout',
  standalone: false,
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
})
export class MainLayout {

  currentYear = new Date().getFullYear();

  userName?: string;
  profilCode?: string;
  userInitials = 'AD';

  constructor(
    private router: Router
  ) {}

  ngOnInit(): void {
    // TODO: plus tard, appeler ton AuthService pour récupérer /auth/me
    // et remplir userName, profilCode, userInitials
  }

  logout(): void {
    // Pour l'instant simple : nettoyer les tokens + rediriger vers login
    // Si tu utilises Keycloak JS adapter, tu peux appeler keycloak.logout()
    this.router.navigate(['/login']);
  }
}
