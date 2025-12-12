import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PasswordResetService } from '../../services/password-reset.service';
import { Subject, catchError, finalize, of, takeUntil } from 'rxjs';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss'],
  standalone: false
})
export class ResetPasswordComponent implements OnInit, OnDestroy {

  form: FormGroup;
  loading = false;
  token?: string;
  tokenInvalid = false;
  hidePassword = true;
  hideConfirmPassword = true;
  currentTheme: 'light' | 'dark' = 'light';
  private readonly destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private passwordResetService: PasswordResetService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  ngOnInit(): void {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark' || savedTheme === 'light') {
      this.applyTheme(savedTheme as 'dark' | 'light');
    } else {
      this.applyTheme('light');
    }

    this.token = this.route.snapshot.queryParamMap.get('token') || undefined;

    if (!this.token) {
      this.setTokenInvalid();
      return;
    }

    this.passwordResetService.verifyToken(this.token)
      .pipe(
        takeUntil(this.destroy$),
        catchError(() => {
          this.setTokenInvalid();
          return of({ valid: false });
        })
      )
      .subscribe(res => {
        if (!res?.valid) {
          this.setTokenInvalid();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setTokenInvalid(): void {
    this.tokenInvalid = true;
    this.snackBar.open('Lien de reinitialisation invalide ou expire', 'Fermer', { duration: 3000 });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  submit(): void {
    if (this.form.invalid || !this.token || this.tokenInvalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.passwordResetService.resetPassword(this.token, this.form.value.password)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
        }),
        catchError(err => {
          this.snackBar.open(
            'Erreur lors de la reinitialisation',
            'Fermer',
            { duration: 3000 }
          );
          return of(void 0);
        })
      )
      .subscribe(() => {
        this.snackBar.open(
          'Mot de passe reinitialise avec succes',
          'Fermer',
          { duration: 3000 }
        );
        this.router.navigate(['/auth/login']);
      });
  }

  goBack(): void {
    this.router.navigate(['/auth/login']);
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
}
