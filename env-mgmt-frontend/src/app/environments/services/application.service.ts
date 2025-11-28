// src/app/environments/services/application.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApplicationDTO, EnvApplicationDTO } from '../models/environment.model';
import {environment} from '../../config/environment';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {

  private readonly baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}

  // Applications globales
  getAllApplications(): Observable<ApplicationDTO[]> {
    return this.http.get<ApplicationDTO[]>(`${this.baseUrl}/applications/actives`);
  }

  // Applications d'un environnement
  getByEnvironment(envId: number): Observable<EnvApplicationDTO[]> {
    return this.http.get<EnvApplicationDTO[]>(`${this.baseUrl}/env-applications/by-env/${envId}`);
  }

  create(payload: Partial<EnvApplicationDTO>): Observable<EnvApplicationDTO> {
    return this.http.post<EnvApplicationDTO>(`${this.baseUrl}/env-applications`, payload);
  }

  update(id: number, payload: Partial<EnvApplicationDTO>): Observable<EnvApplicationDTO> {
    return this.http.put<EnvApplicationDTO>(`${this.baseUrl}/env-applications/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/env-applications/${id}`);
  }
}
