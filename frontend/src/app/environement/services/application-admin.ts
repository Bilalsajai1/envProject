import { Injectable } from '@angular/core';
import {environment} from '../../../configuration/environement.config';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Application} from '../models/application.model';
@Injectable({
  providedIn: 'root'
})
export class ApplicationAdminService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications`);
  }

  getApplication(id: number): Observable<Application> {
    return this.http.get<Application>(`${this.apiUrl}/applications/${id}`);
  }

  createApplication(payload: Partial<Application>): Observable<Application> {
    return this.http.post<Application>(`${this.apiUrl}/applications`, payload);
  }

  updateApplication(id: number, payload: Partial<Application>): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${id}`, payload);
  }

  deleteApplication(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/applications/${id}`);
  }
}
