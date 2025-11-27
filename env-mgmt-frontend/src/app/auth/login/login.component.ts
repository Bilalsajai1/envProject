import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';
import { AuthContextService } from '../services/auth-context.service';

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
          next: () => {
            this.loading = false;
            this.router.navigate(['/admin/users']);
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
}
