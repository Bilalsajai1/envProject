import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import {AuthService} from '../auth-service';

@Component({
  selector: 'app-auth-callback',
  standalone: false,
  template: '<p>Authentification...</p>'
})
export class AuthCallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');

    if (!code) {
      console.error('No code found in callback');
      return;
    }

    const body = new HttpParams()
      .set('grant_type', 'authorization_code')
      .set('client_id', 'angular-client')
      .set('code', code)
      .set('redirect_uri', 'http://localhost:4200/auth/callback');

    this.http.post<any>(
      'http://localhost:8080/realms/env-mgmt/protocol/openid-connect/token',
      body,
      {
        headers: new HttpHeaders({
          'Content-Type': 'application/x-www-form-urlencoded'
        })
      }
    ).subscribe({
      next: async (res) => {

        this.auth.saveToken(res.access_token);

        this.auth.init().subscribe({
          next: () => this.router.navigate(['/dashboard']),
          error: () => this.router.navigate(['/'])
        });
      },
      error: err => console.error(err)
    });
    this.router.navigate(['/admin/users']);
  }
}
