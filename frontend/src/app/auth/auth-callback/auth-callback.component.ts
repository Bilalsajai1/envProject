import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';

@Component({
  selector: 'app-auth-callback',
  standalone: false,
  template: `<p>Authentification...</p>`
})
export class AuthCallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {

    const code = this.route.snapshot.queryParamMap.get('code');
    if (!code) {
      console.error("No code received");
      return;
    }

    const body = new HttpParams()
      .set('grant_type', 'authorization_code')
      .set('client_id', 'angular-client')
      .set('code', code)
      .set('redirect_uri', 'http://localhost:4200/');

    this.http.post<any>(
      'http://localhost:8080/realms/env-mgmt/protocol/openid-connect/token',
      body
    ).subscribe({
      next: (res) => {
        localStorage.setItem('kc_token', res.access_token);
        this.router.navigate(['/']);
      },
      error: (err) => console.error(err)
    });
  }
}
