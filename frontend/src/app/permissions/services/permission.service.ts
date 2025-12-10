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

  getProfils(): Observable<ProfilSimple[]> {
    return this.http.get<ProfilSimple[]>(this.baseUrl);
  }

  getPermissions(profilId: number): Observable<ProfilPermissions> {
    return this.http
      .get<any>(`${this.baseUrl}/${profilId}/permissions`)
      .pipe(
        map((res: any) => {
          console.log('🔍 Réponse backend GET /profils/{id}/permissions:', res);

          // ✅ Types d'environnement avec allowedActions
          const rawEnvTypes = res.environmentTypes ?? [];
          const envTypePermissions: EnvTypePermission[] = rawEnvTypes.map((et: any) => ({
            typeCode: et.code,
            typeLibelle: et.libelle,
            allowedActions: (et.allowedActions ?? []) as ActionType[]
          }));

          // ✅ Projets
          const rawProjects = res.projects ?? [];
          const projectPermissions: ProjectPermission[] = rawProjects.map((p: any) => ({
            projectId: p.id,
            projectCode: p.code,
            projectLibelle: p.libelle,
            actions: (p.allowedActions ?? []) as ActionType[],
            environmentTypeCodes: p.environmentTypeCodes ?? []
          }));

          const profilPermissions: ProfilPermissions = {
            profilId: res.profilId,
            profilCode: res.profilCode,
            profilLibelle: res.profilLibelle,
            isAdmin: res.isAdmin ?? false, // ✅ AJOUTER cette ligne
            envTypePermissions,
            projectPermissions
          };

          console.log('✅ ProfilPermissions normalisé:', profilPermissions);
          return profilPermissions;
        })
      );
  }

  savePermissions(
    profilId: number,
    envUpdates: EnvTypePermissionUpdate[],
    projUpdates: ProjectPermissionUpdate[]
  ): Observable<void> {
    const payload = {
      profilId: profilId,
      envTypePermissions: envUpdates,
      projectPermissions: projUpdates
    };

    console.log('📤 Envoi PUT /profils/{id}/permissions:', payload);

    return this.http.put<void>(
      `${this.baseUrl}/${profilId}/permissions`,
      payload
    );
  }
}
