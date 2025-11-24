import { Injectable } from '@angular/core';
import {environment} from '../../../configuration/environement.config';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EnvApplication} from '../models/env-application.model';

@Injectable({
  providedIn: 'root'
})
export class EnvApplicationService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getApplicationsForEnv(envId: number): Observable<EnvApplication[]> {
    return this.http.get<EnvApplication[]>(
      `${this.apiUrl}/env-applications/by-env/${envId}`
    );
  }

  createApplication(dto: Partial<EnvApplication>): Observable<EnvApplication> {
    return this.http.post<EnvApplication>(
      `${this.apiUrl}/env-applications`,
      dto
    );
  }

  updateApplication(id: number, dto: Partial<EnvApplication>): Observable<EnvApplication> {
    return this.http.put<EnvApplication>(
      `${this.apiUrl}/env-applications/${id}`,
      dto
    );
  }

  deleteApplication(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/env-applications/${id}`
    );
  }
}
