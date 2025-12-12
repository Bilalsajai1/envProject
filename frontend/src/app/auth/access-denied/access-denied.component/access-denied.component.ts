
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import {AuthenticationService} from '../../services/authentication.service';

@Component({
  selector: 'app-access-denied',
  standalone: false,
  template: `
    <div class="access-denied-container">
      <mat-card class="access-denied-card">
        <mat-card-content>
          <div class="icon-container">
            <mat-icon color="warn">block</mat-icon>
          </div>

          <h1>Accès Refusé</h1>

          <p class="message">
            Votre compte ne possède pas les permissions nécessaires pour accéder à cette application.
          </p>

          <p class="hint">
            Veuillez contacter votre administrateur pour obtenir les accès appropriés.
          </p>

          <div class="actions">
            <button mat-stroked-button (click)="goBack()">
              <mat-icon>arrow_back</mat-icon>
              Retour
            </button>

            <button mat-raised-button color="primary" (click)="logout()">
              <mat-icon>logout</mat-icon>
              Se déconnecter
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .access-denied-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 1rem;
    }

    .access-denied-card {
      max-width: 500px;
      width: 100%;
      text-align: center;
    }

    .icon-container {
      margin-bottom: 1.5rem;

      mat-icon {
        font-size: 80px;
        width: 80px;
        height: 80px;
      }
    }

    h1 {
      font-size: 2rem;
      font-weight: 500;
      margin-bottom: 1rem;
      color: rgba(0, 0, 0, 0.87);
    }

    .message {
      font-size: 1.1rem;
      margin-bottom: 1rem;
      color: rgba(0, 0, 0, 0.7);
    }

    .hint {
      font-size: 0.95rem;
      color: rgba(0, 0, 0, 0.6);
      margin-bottom: 2rem;
    }

    .actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      flex-wrap: wrap;
    }
  `]
})
export class AccessDeniedComponent {

  constructor(
    private auth: AuthenticationService,
    private router: Router
  ) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
