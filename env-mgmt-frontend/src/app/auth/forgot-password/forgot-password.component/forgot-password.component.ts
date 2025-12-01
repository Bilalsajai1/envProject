// src/app/auth/forgot-password/forgot-password.component.ts

import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import {PasswordResetService} from '../../services/password-reset.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
  standalone: false
})
export class ForgotPasswordComponent {

  form: FormGroup;
  loading = false;
  success = false;

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

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.passwordResetService.requestReset(this.form.value.email).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        this.snackBar.open(
          '✅ Un email de réinitialisation a été envoyé',
          'Fermer',
          { duration: 5000 }
        );
      },
      error: (err) => {
        this.loading = false;
        this.snackBar.open(
          '❌ Erreur lors de l\'envoi de l\'email',
          'Fermer',
          { duration: 3000 }
        );
        console.error(err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/auth/login']);
  }
}
