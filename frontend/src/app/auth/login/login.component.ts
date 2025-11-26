import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth-service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {

  loading = false;
  year = new Date().getFullYear();

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Si déjà connecté → on ne reste pas sur la page de login
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  login(): void {
    this.loading = true;
    this.auth.redirectToLogin();
  }
}
