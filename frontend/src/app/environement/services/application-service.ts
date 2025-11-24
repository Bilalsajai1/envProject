import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {Application} from '../models/application.model';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../configuration/environement.config';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications`);
  }

  getActives(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications/actives`);
  }

  create(app: Partial<Application>): Observable<Application> {
    return this.http.post<Application>(`${this.apiUrl}/applications`, app);
  }

  update(id: number, app: Partial<Application>): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${id}`, app);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/applications/${id}`);
  }
}
