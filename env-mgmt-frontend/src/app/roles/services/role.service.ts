// src/app/roles/services/role.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../config/environment';

// Interfaces de base pour la pagination
export interface PaginationRequest {
  page: number;
  size: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface RoleDTO {
  id: number;
  code: string;
  libelle: string;
  action: string;
  actif: boolean;
  menuId?: number;
  menuCode?: string;
  menuLibelle?: string;
  environnementId?: number;
  environnementCode?: string;
  environnementLibelle?: string;
  projetId?: number;
  projetCode?: string;
  projetLibelle?: string;
  createdAt?: string;
  updatedAt?: string;
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

export interface MenuDTO {
  id: number;
  code: string;
  libelle: string;
}

export interface EnvironnementDTO {
  id: number;
  code: string;
  libelle: string;
}

export interface ProjetDTO {
  id: number;
  code: string;
  libelle: string;
}

// Interface corrigée avec la propriété search
export interface RoleSearchRequest extends PaginationRequest {
  search?: string;  // ← Ajout de la propriété search
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

  search(req: RoleSearchRequest): Observable<PaginatedResponse<RoleDTO>> {
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

  getByProjet(projetId: number): Observable<RoleDTO[]> {
    return this.http.get<RoleDTO[]>(`${this.baseUrl}/by-projet/${projetId}`);
  }

  // Récupérer les menus disponibles
  getMenus(): Observable<MenuDTO[]> {
    return this.http.get<MenuDTO[]>(`${environment.apiUrl}/menus`);
  }

  // Récupérer les environnements disponibles
  getEnvironnements(): Observable<EnvironnementDTO[]> {
    return this.http.get<EnvironnementDTO[]>(`${environment.apiUrl}/environnements`);
  }

  // Récupérer les projets disponibles
  getProjets(): Observable<ProjetDTO[]> {
    return this.http.get<ProjetDTO[]>(`${environment.apiUrl}/projets`);
  }

  // Export vers Excel
  exportToExcel(req: RoleSearchRequest): Observable<Blob> {
    return this.http.post(`${this.baseUrl}/export/excel`, req, {
      responseType: 'blob'
    });
  }
}
