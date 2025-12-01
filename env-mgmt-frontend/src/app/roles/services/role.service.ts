// src/app/roles/services/role.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../config/environment';
import { PaginatedResponse, PaginationRequest } from '../../users/models/user.model';

export interface RoleDTO {
  id: number;
  code: string;
  libelle: string;
  action: string;
  actif: boolean;
  menuId?: number;
  menuCode?: string;
  environnementId?: number;
  environnementCode?: string;
  projetId?: number;
  projetCode?: string;
}

export interface RoleCreateUpdateDTO {
  code: string;
  libelle: string;
  action: string;
  actif?: boolean;
  menuId?: number;
  environnementId?: number;
  projetId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private readonly baseUrl = `${environment.apiUrl}/roles`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<RoleDTO[]> {
    return this.http.get<RoleDTO[]>(this.baseUrl);
  }

  getById(id: number): Observable<RoleDTO> {
    return this.http.get<RoleDTO>(`${this.baseUrl}/${id}`);
  }

  create(payload: RoleCreateUpdateDTO): Observable<RoleDTO> {
    return this.http.post<RoleDTO>(this.baseUrl, payload);
  }

  update(id: number, payload: RoleCreateUpdateDTO): Observable<RoleDTO> {
    return this.http.put<RoleDTO>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  search(req: PaginationRequest): Observable<PaginatedResponse<RoleDTO>> {
    return this.http.post<PaginatedResponse<RoleDTO>>(
      `${this.baseUrl}/search`,
      req
    );
  }

  getByMenu(menuId: number): Observable<RoleDTO[]> {
    return this.http.get<RoleDTO[]>(`${this.baseUrl}/by-menu/${menuId}`);
  }

  getByEnvironnement(envId: number): Observable<RoleDTO[]> {
    return this.http.get<RoleDTO[]>(`${this.baseUrl}/by-env/${envId}`);
  }
}
