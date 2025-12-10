// src/app/auth/forgot-password/forgot-password.component.ts
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil, finalize } from 'rxjs';
import {PasswordResetService} from '../../services/password-reset.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
  standalone: false
})
export class ForgotPasswordComponent implements OnInit, OnDestroy {
  form: FormGroup;
  loading = false;
  success = false;
  currentTheme: 'light' | 'dark' = 'light';
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private passwordResetService: PasswordResetService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark' || savedTheme === 'light') {
      this.applyTheme(savedTheme as 'dark' | 'light');
    } else {
      this.applyTheme('light');
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.passwordResetService.requestReset(this.form.value.email)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: () => {
          this.success = true;
          this.snackBar.open(
            'Un email de réinitialisation a été envoyé',
            'Fermer',
            { duration: 5000, panelClass: ['success-snackbar'] }
          );
        },
        error: (err) => {
          const message = err.status === 404
            ? 'Aucun compte associé à cet email'
            : 'Erreur lors de l\'envoi de l\'email';

          this.snackBar.open(message, 'Fermer', {
            duration: 4000,
            panelClass: ['error-snackbar']
          });
          console.error('[ForgotPassword] Erreur:', err);
        }
      });
  }

  goBack(): void {
    this.router.navigate(['/auth/login']);
  }
}
