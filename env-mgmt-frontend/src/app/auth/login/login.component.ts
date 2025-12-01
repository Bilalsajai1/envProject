// src/app/auth/login/login.component.ts

import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';
import { AuthContextService } from '../services/auth-context.service';
import { AuthContext } from '../models/auth-context.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent {

  form: FormGroup;
  loading = false;
  error?: string;

  constructor(
    private fb: FormBuilder,
    private auth: AuthenticationService,
    private authCtx: AuthContextService,
    private router: Router
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = undefined;

    this.auth.login(this.form.value).subscribe({
      next: () => {
        // une fois loggé → charger /auth/me
        this.authCtx.loadAuthContext().subscribe({
          next: (ctx) => {
            this.loading = false;
            const target = this.getDefaultRoute(ctx);
            this.router.navigate([target]);
          },
          error: () => {
            this.loading = false;
            this.error = 'Impossible de charger le contexte utilisateur';
          }
        });
      },
      error: err => {
        this.loading = false;
        this.error = 'Identifiants invalides';
        console.error(err);
      }
    });
  }

  // 🔁 Choix de la route par défaut selon le contexte
  private getDefaultRoute(ctx: AuthContext | null): string {
    if (!ctx || !ctx.user) {
      return '/auth/login';
    }

    const roles = ctx.user.roles ?? [];

    // 1️⃣ Admin ou rôle d’accès aux utilisateurs → vue admin des users
    if (ctx.user.admin || roles.includes('ROLE_USERS_ACCESS')) {
      return '/admin/users';
    }

    // 2️⃣ Sinon, on cherche le 1er type d'environnement avec CONSULT
    const env = ctx.environmentTypes?.find(t =>
      t.allowedActions?.includes('CONSULT')
    );

    if (env) {
      return `/env/${env.code.toLowerCase()}`;
    }

    // 3️⃣ Fallback : rien d’autorisé → on revient sur login (plus tard page 403)
    return '/auth/login';
  }
}
