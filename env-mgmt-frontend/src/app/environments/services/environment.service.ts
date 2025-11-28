// src/app/environments/services/environment.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnvironmentDTO } from '../models/environment.model';
import {environment} from '../../config/environment';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  private readonly baseUrl = `${environment.apiUrl}/environnements`;

  constructor(private http: HttpClient) {}

  getByProjectAndType(projectId: number, typeCode: string): Observable<EnvironmentDTO[]> {
    const params = new HttpParams()
      .set('projetId', projectId.toString())
      .set('typeCode', typeCode);

    return this.http.get<EnvironmentDTO[]>(this.baseUrl, { params });
  }

  create(payload: Partial<EnvironmentDTO>): Observable<EnvironmentDTO> {
    return this.http.post<EnvironmentDTO>(this.baseUrl, payload);
  }

  update(id: number, payload: Partial<EnvironmentDTO>): Observable<EnvironmentDTO> {
    return this.http.put<EnvironmentDTO>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
