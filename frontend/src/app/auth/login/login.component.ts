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

    if (rememberMe) {
      localStorage.setItem('remembered_username', username);
    } else {
      localStorage.removeItem('remembered_username');
    }

    this.auth.login({ username, password }).subscribe({
      next: () => {
        this.authCtx.loadAuthContext().subscribe({
          next: (ctx) => {
            this.loading = false;
            const target = this.getDefaultRoute(ctx);
            console.log('🚀 Redirection vers:', target);
            this.router.navigate([target]);
          },
          error: (err) => {
            this.loading = false;
            this.error = 'Impossible de charger le contexte utilisateur';
            console.error('❌ Erreur chargement contexte:', err);
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
        console.error('❌ Erreur login:', err);
      }
    });
  }

  goToForgotPassword(): void {
    this.router.navigate(['/auth/forgot-password']);
  }

  /**
   * ✅ NOUVELLE LOGIQUE avec EnvironmentTypeWithProjects
   */
  private getDefaultRoute(ctx: AuthContext | null): string {
    if (!ctx || !ctx.user) {
      console.warn('⚠️ Pas de contexte utilisateur, retour au login');
      return '/auth/login';
    }

    const roles = ctx.user.roles ?? [];

    // 1️⃣ Admin ou rôle d'accès aux utilisateurs → vue admin
    if (ctx.user.admin || roles.includes('ROLE_USERS_ACCESS')) {
      console.log('✅ Utilisateur admin, redirection vers /admin/users');
      return '/admin/users';
    }

    // 2️⃣ User normal → chercher le premier type d'environnement accessible
    const envTypes = ctx.environmentTypes ?? [];

    for (const envType of envTypes) {
      const projects = envType.projects ?? [];

      // Vérifier si l'utilisateur a au moins un projet avec CONSULT
      const hasAccessibleProject = projects.some(p =>
        p.allowedActions && p.allowedActions.includes('CONSULT')
      );

      if (hasAccessibleProject) {
        const route = `/env/${envType.code.toLowerCase()}`;
        console.log(`✅ Accès trouvé au type ${envType.code}, redirection vers ${route}`);
        return route;
      }
    }

    // 3️⃣ Aucun accès trouvé
    console.warn('⚠️ Aucun environnement accessible trouvé');
    return '/auth/access-denied';
  }
}
