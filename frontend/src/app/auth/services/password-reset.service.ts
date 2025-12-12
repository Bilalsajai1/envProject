import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../config/environment';

@Injectable({
  providedIn: 'root'
})
export class PasswordResetService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  requestReset(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/reset-password`, {
      token,
      newPassword
    });
  }

  verifyToken(token: string): Observable<{ valid: boolean }> {
    return this.http.get<{ valid: boolean }>(`${this.baseUrl}/auth/verify-reset-token`, {
      params: { token }
    });
  }
}
