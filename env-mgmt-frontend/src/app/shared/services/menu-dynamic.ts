// src/app/shared/services/menu-dynamic.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../config/environment';

export interface MenuDTO {
  id: number;
  code: string;
  libelle: string;
  route?: string;
  icon?: string;
  ordre?: number;
  visible: boolean;
  parentId?: number;
  parentCode?: string;
  environmentTypeId?: number;
  environmentTypeCode?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MenuDynamicService {

  private readonly baseUrl = `${environment.apiUrl}/api/menus-dynamic`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère les menus accessibles pour l'utilisateur connecté
   */
  getMyMenus(): Observable<MenuDTO[]> {
    return this.http.get<MenuDTO[]>(this.baseUrl);
  }

  /**
   * Récupère les menus pour un type d'environnement spécifique
   */
  getMenusForType(typeCode: string): Observable<MenuDTO[]> {
    return this.http.get<MenuDTO[]>(`${this.baseUrl}/by-type/${typeCode}`);
  }
}
