import { Component, OnDestroy } from '@angular/core';
import { Location } from '@angular/common';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subject, takeUntil, finalize } from 'rxjs';
import { AuthenticationService } from '../../auth/services/authentication.service';

@Component({
  selector: 'app-change-password',
  standalone: false,
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnDestroy {

  form: FormGroup;
  hideCurrent = true;
  hideNew = true;
  hideConfirm = true;
  saving = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private auth: AuthenticationService,
    private snackBar: MatSnackBar,
    public router: Router,
    private location: Location
  ) {
    this.form = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordsMatchValidator
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { currentPassword, newPassword } = this.form.value;
    this.saving = true;

    this.auth.changePassword(currentPassword, newPassword)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.saving = false;
        })
      )
      .subscribe({
        next: () => {
          this.auth.logout();
          this.snackBar.open('Mot de passe mis à jour. Veuillez vous reconnecter.', 'Fermer', { duration: 4000 });
          this.router.navigate(['/auth/login']);
        },
        error: (err) => {
          const message = err?.error?.message || 'Impossible de changer le mot de passe';
          this.snackBar.open(message, 'Fermer', { duration: 4000 });
        }
      });
  }

  goBack(): void {
    this.location.back();
  }

  toggle(field: 'current' | 'new' | 'confirm'): void {
    if (field === 'current') this.hideCurrent = !this.hideCurrent;
    if (field === 'new') this.hideNew = !this.hideNew;
    if (field === 'confirm') this.hideConfirm = !this.hideConfirm;
  }

  get currentPassword(): AbstractControl | null { return this.form.get('currentPassword'); }
  get newPassword(): AbstractControl | null { return this.form.get('newPassword'); }
  get confirmPassword(): AbstractControl | null { return this.form.get('confirmPassword'); }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const newPass = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    if (!newPass || !confirm) {
      return null;
    }
    return newPass === confirm ? null : { passwordMismatch: true };
  }
}
