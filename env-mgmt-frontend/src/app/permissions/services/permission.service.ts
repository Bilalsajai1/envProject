// src/app/permissions/services/permission.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import {
  ProfilPermissions,
  ProfilSimple,
  EnvTypePermission,
  ActionType,
  EnvTypePermissionUpdate,
  ProjectPermissionUpdate,
  ProjectPermission
} from '../models/permission.model';
import { environment } from '../../config/environment';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {

  private readonly baseUrl = `${environment.apiUrl}/profils`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère la liste des profils
   */
  getProfils(): Observable<ProfilSimple[]> {
    return this.http.get<ProfilSimple[]>(this.baseUrl);
  }

  /**
   * Récupère les permissions d'un profil
   * ✅ Compatible avec le nouveau backend
   */
  getPermissions(profilId: number): Observable<ProfilPermissions> {
    return this.http
      .get<any>(`${this.baseUrl}/${profilId}/permissions`)
      .pipe(
        map((res: any) => {
          console.log('🔍 Réponse backend /profils/{id}/permissions :', res);

          // ✅ Types d'environnement
          const rawEnvTypes = res.environmentTypes ?? [];
          const envTypePermissions: EnvTypePermission[] = rawEnvTypes.map((et: any) => ({
            typeCode: et.code,
            typeLibelle: et.libelle,
            actions: (et.allowedActions ?? []) as ActionType[]
          }));

          // ✅ Projets
          const rawProjects = res.projects ?? [];
          const projectPermissions: ProjectPermission[] = rawProjects.map((p: any) => ({
            projectId: p.id,
            projectCode: p.code,
            projectLibelle: p.libelle,
            actions: (p.allowedActions ?? []) as ActionType[]
          }));

          const profilPermissions: ProfilPermissions = {
            profilId: res.profilId,
            profilCode: res.profilCode,
            profilLibelle: res.profilLibelle,
            envTypePermissions,
            projectPermissions
          };

          console.log('✅ ProfilPermissions normalisé côté front :', profilPermissions);
          return profilPermissions;
        })
      );
  }

  /**
   * ✅ NOUVELLE VERSION : Utilise l'endpoint consolidé PUT /profils/{id}/permissions
   */
  savePermissions(
    profilId: number,
    envUpdates: EnvTypePermissionUpdate[],
    projUpdates: ProjectPermissionUpdate[]
  ): Observable<void> {
    const payload = {
      profilId,
      envTypePermissions: envUpdates,
      projectPermissions: projUpdates
    };

    console.log('📤 Envoi des permissions au backend :', payload);

    return this.http.put<void>(
      `${this.baseUrl}/${profilId}/permissions`,
      payload
    );
  }
}
