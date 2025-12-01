import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, forkJoin } from 'rxjs';
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
          console.log('🔍 Réponse brute /profils/{id}/permissions :', res);

          // 1️⃣ Types d'environnement : envTypePermissions OU environmentTypes
          const rawEnvTypes = res.envTypePermissions ?? res.environmentTypes ?? [];
          const envTypePermissions: EnvTypePermission[] = (rawEnvTypes as any[]).map(et => ({
            typeCode: et.typeCode ?? et.code,
            typeLibelle: et.typeLibelle ?? et.libelle,
            actions: (et.actions ?? et.allowedActions ?? []) as ActionType[]
          }));

          // 2️⃣ Projets : backend renvoie "projects"
          const rawProjects = res.projects ?? [];
          const projectPermissions: ProjectPermission[] = (rawProjects as any[]).map((p: any) => ({
            projectId: p.id,
            projectCode: p.code,
            projectLibelle: p.libelle,
            actions: (p.actions ?? p.allowedActions ?? []) as ActionType[]
          }));

          const profilPermissions: ProfilPermissions = {
            profilId: res.profilId,
            profilCode: res.profilCode,
            profilLibelle: res.profilLibelle,
            envTypePermissions,
            projectPermissions,
            // on laisse ces champs vides pour que les *ngIf éventuels restent safe
            projectActions: [],
            environmentActions: []
          };

          console.log('✅ ProfilPermissions normalisé côté front :', profilPermissions);
          return profilPermissions;
        })
      );
  }

  /** 🔥 Nouvelle version : on pousse envTypes + projets sur 2 endpoints backend */
  savePermissions(
    profilId: number,
    envUpdates: EnvTypePermissionUpdate[],
    projUpdates: ProjectPermissionUpdate[]
  ): Observable<void> {
    const env$ = this.http.put<void>(
      `${this.baseUrl}/${profilId}/permissions/env-types`,
      envUpdates
    );

    const proj$ = this.http.put<void>(
      `${this.baseUrl}/${profilId}/permissions/projects`,
      projUpdates
    );

    return forkJoin([env$, proj$]).pipe(
      map(() => void 0)
    );
  }
}
