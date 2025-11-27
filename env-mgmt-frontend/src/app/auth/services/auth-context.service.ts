import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthContext } from '../models/auth-context.model';
import { environment } from '../../config/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthContextService {

  private authContextSubject = new BehaviorSubject<AuthContext | null>(null);
  authContext$ = this.authContextSubject.asObservable();

  private loaded = false;

  constructor(private http: HttpClient) {}

  loadAuthContext(): Observable<AuthContext> {
    return this.http
      .get<AuthContext>(`${environment.apiUrl}/auth/me`)
      .pipe(
        tap(ctx => {
          this.loaded = true;
          this.authContextSubject.next(ctx);
        })
      );
  }

  ensureLoaded(): Observable<AuthContext | null> {
    if (this.loaded && this.authContextSubject.value) {
      return of(this.authContextSubject.value);
    }
    return this.loadAuthContext();
  }

  getSnapshot(): AuthContext | null {
    return this.authContextSubject.value;
  }
}
