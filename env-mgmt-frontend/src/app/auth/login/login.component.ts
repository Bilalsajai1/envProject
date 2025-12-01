// src/app/auth/login/login.component.ts

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';
import { AuthContextService } from '../services/auth-context.service';
import { SessionStorageService } from '../services/session-storage.service';
import { AuthContext } from '../models/auth-context.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent implements OnInit {

  form: FormGroup;
  loading = false;
  error?: string;
  hidePassword = true;

  constructor(
    private fb: FormBuilder,
    private auth: AuthenticationService,
    private authCtx: AuthContextService,
    private sessionStorage: SessionStorageService,
    private router: Router
  ) {
    this.form = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  ngOnInit(): void {
    // ✅ Charger le username sauvegardé si "Remember Me" était coché
    const savedUsername = localStorage.getItem('remembered_username');
    if (savedUsername) {
      this.form.patchValue({
        username: savedUsername,
        rememberMe: true
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = undefined;

    const { username, password, rememberMe } = this.form.value;

    // ✅ Gérer Remember Me
    if (rememberMe) {
      localStorage.setItem('remembered_username', username);
    } else {
      localStorage.removeItem('remembered_username');
    }

    this.auth.login({ username, password }).subscribe({
      next: () => {
        // Une fois loggé → charger /auth/me
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
        if (err.status === 401) {
          this.error = 'Identifiants invalides. Veuillez réessayer.';
        } else {
          this.error = 'Une erreur est survenue. Veuillez réessayer plus tard.';
        }
        console.error(err);
      }
    });
  }

  goToForgotPassword(): void {
    this.router.navigate(['/auth/forgot-password']);
  }

  // 🔁 Choix de la route par défaut selon le contexte
  private getDefaultRoute(ctx: AuthContext | null): string {
    if (!ctx || !ctx.user) {
      return '/auth/login';
    }

    const roles = ctx.user.roles ?? [];

    // 1️⃣ Admin ou rôle d'accès aux utilisateurs → vue admin des users
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

    // 3️⃣ Fallback
    return '/auth/login';
  }
}
