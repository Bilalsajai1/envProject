import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, catchError, finalize, of, switchMap, takeUntil } from 'rxjs';
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
export class LoginComponent implements OnInit, OnDestroy {

  form: FormGroup;
  loading = false;
  error?: string;
  hidePassword = true;
  currentTheme: 'light' | 'dark' = 'light';
  private readonly destroy$ = new Subject<void>();

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
    document.body.classList.add('auth-login-page');

    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark' || savedTheme === 'light') {
      this.applyTheme(savedTheme as 'dark' | 'light');
    } else {
      this.applyTheme('light');
    }

    const savedUsername = localStorage.getItem('remembered_username');
    if (savedUsername) {
      this.form.patchValue({
        username: savedUsername,
        rememberMe: true
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    document.body.classList.remove('auth-login-page');
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

    this.auth.login({ username, password }).pipe(
      switchMap(() => this.authCtx.loadAuthContext()),
      takeUntil(this.destroy$),
      finalize(() => {
        this.loading = false;
      }),
      catchError(err => {
        if (err?.status === 401) {
          this.error = 'Identifiants invalides. Veuillez reessayer.';
        } else {
          this.error = 'Une erreur est survenue. Veuillez reessayer plus tard.';
        }
        console.error('Erreur login/contexte:', err);
        return of(null);
      })
    ).subscribe(ctx => {
      if (!ctx) return;
      const target = this.getDefaultRoute(ctx);
      this.router.navigate([target]);
    });
  }

  goToForgotPassword(): void {
    this.router.navigate(['/auth/forgot-password']);
  }

  toggleTheme(): void {
    const next = this.currentTheme === 'light' ? 'dark' : 'light';
    this.applyTheme(next);
  }

  private applyTheme(theme: 'light' | 'dark') {
    this.currentTheme = theme;
    document.documentElement.setAttribute('data-theme', theme);
    document.body.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }

  private getDefaultRoute(ctx: AuthContext | null): string {
    if (!ctx || !ctx.user) {
      return '/auth/login';
    }

    const roles = ctx.user.roles ?? [];

    if (ctx.user.admin || roles.includes('ROLE_USERS_ACCESS')) {
      return '/admin/users';
    }

    const envTypes = ctx.environmentTypes ?? [];
    for (const envType of envTypes) {
      const projects = envType.projects ?? [];
      const hasAccessibleProject = projects.some(p =>
        p.allowedActions && p.allowedActions.includes('CONSULT')
      );

      if (hasAccessibleProject) {
        return `/env/${envType.code.toLowerCase()}`;
      }
    }

    return '/auth/access-denied';
  }
}
