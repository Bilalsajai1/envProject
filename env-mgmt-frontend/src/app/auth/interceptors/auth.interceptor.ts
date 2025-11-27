import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { SessionStorageService } from '../services/session-storage.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private session: SessionStorageService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const token = this.session.getAccessToken();
    let authReq = req;

    if (token) {
      authReq = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.session.clear();
          this.router.navigate(['/auth/login']);
        }
        return throwError(() => error);
      })
    );
  }
}
