// src/app/permissions/services/permission.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProfilPermissions, SavePermissionsRequest, ProfilSimple } from '../models/permission.model';
import {environment} from '../../config/environment';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  private readonly baseUrl = `${environment.apiUrl}/profils`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère la liste des profils actifs
   */
  getProfils(): Observable<ProfilSimple[]> {
    return this.http.get<ProfilSimple[]>(this.baseUrl);
  }

  /**
   * Récupère les permissions d'un profil
   */
  getPermissions(profilId: number): Observable<ProfilPermissions> {
    return this.http.get<ProfilPermissions>(`${this.baseUrl}/${profilId}/permissions`);
  }

  /**
   * Sauvegarde les permissions d'un profil
   */
  savePermissions(profilId: number, request: SavePermissionsRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${profilId}/permissions`, request);
  }
}
