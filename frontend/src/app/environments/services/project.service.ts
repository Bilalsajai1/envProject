import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectDTO } from '../models/environment.model';
import { environment } from '../../config/environment';

export type SortDirection = 'asc' | 'desc';

export interface PaginationRequest {
  page: number;
  size: number;
  sortField: string;
  sortDirection: SortDirection;
  filters: Record<string, any>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  private readonly baseUrl = `${environment.apiUrl}/projects`;

  constructor(private http: HttpClient) {}

  search(req: PaginationRequest): Observable<PaginatedResponse<ProjectDTO>> {
    const url = `${this.baseUrl}/search`;
    return this.http.post<PaginatedResponse<ProjectDTO>>(url, req);
  }
  getByEnvironmentType(typeCode: string): Observable<ProjectDTO[]> {
    return this.http.get<ProjectDTO[]>(`${this.baseUrl}/by-environment-type/${typeCode}`);
  }

  getById(id: number): Observable<ProjectDTO> {
    return this.http.get<ProjectDTO>(`${this.baseUrl}/${id}`);
  }

  create(payload: Partial<ProjectDTO>): Observable<ProjectDTO> {
    return this.http.post<ProjectDTO>(this.baseUrl, payload);
  }

  update(id: number, payload: Partial<ProjectDTO>): Observable<ProjectDTO> {
    return this.http.put<ProjectDTO>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
