// app.ts
import { Component, OnInit } from '@angular/core';
import { AuthService } from './auth/auth-service';

@Component({
  selector: 'app-root',
  standalone: false,
  templateUrl: './app.html'
})
export class App implements OnInit {

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    const token = this.auth.getToken();
    if (token) {
      this.auth.init().subscribe({
        next: () => console.log('User context loaded'),
        error: (err) => console.warn('Error loading user context', err)
      });
    } else {
      console.log('Not authenticated yet');
    }
  }
}
