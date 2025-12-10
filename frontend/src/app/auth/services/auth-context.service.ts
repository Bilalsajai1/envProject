// src/app/auth/services/auth-context.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, map, catchError } from 'rxjs/operators';
import {
  AuthContext,
  EnvironmentTypeWithProjects,
  ProjectWithActions,
  ActionType
} from '../models/auth-context.model';
import { environment } from '../../config/environment';

@Injectable({ providedIn: 'root' })
export class AuthContextService {

  private readonly apiUrl = `${environment.apiUrl}/auth/me`;
  private context$ = new BehaviorSubject<AuthContext | null>(null);

  constructor(private http: HttpClient) {}

  /**
   * ✅ Charge le contexte depuis /auth/me et mappe vers le modèle frontend
   */
  loadContext(): Observable<AuthContext> {
    return this.http.get<any>(this.apiUrl).pipe(
      map(response => this.mapBackendToFrontend(response)),
      tap(ctx => {
        console.log('✅ Contexte chargé:', ctx);
        this.context$.next(ctx);
      }),
      catchError(err => {
        console.error('❌ Erreur chargement contexte:', err);
        throw err;
      })
    );
  }

  /**
   * ✅ Alias pour loadContext (compatibilité)
   */
  loadAuthContext(): Observable<AuthContext> {
    return this.loadContext();
  }

  /**
   * ✅ Assure que le contexte est chargé
   */
  ensureLoaded(): Observable<AuthContext | null> {
    const current = this.context$.value;
    if (current) {
      return of(current);
    }

    return this.loadContext().pipe(
      map(ctx => ctx),
      catchError(err => {
        console.error('❌ Erreur ensureLoaded:', err);
        this.context$.next(null);
        return of(null);
      })
    );
  }

  /**
   * ✅ Observable du contexte
   */
  getContext$(): Observable<AuthContext | null> {
    return this.context$.asObservable();
  }

  /**
   * ✅ Snapshot du contexte actuel
   */
  getCurrentContext(): AuthContext | null {
    return this.context$.value;
  }

  /**
   * ✅ Vérifie si l'utilisateur peut voir un type d'environnement
   */
  canViewType(typeCode: string): boolean {
    const ctx = this.context$.value;
    if (!ctx) return false;

    if (ctx.user?.admin) return true;

    const envType = ctx.environmentTypes?.find(
      t => t.code.toUpperCase() === typeCode.toUpperCase()
    );

    if (!envType) return false;

    // Un type est visible si l'utilisateur a au moins un projet avec CONSULT
    return envType.projects.some(p =>
      p.allowedActions.includes('CONSULT')
    );
  }

  /**
   * ✅ Vérifie si l'utilisateur peut faire une action sur un projet
   */
  canAccessProject(projectId: number, action: ActionType): boolean {
    const ctx = this.context$.value;
    if (!ctx) return false;

    if (ctx.user?.admin) return true;

    for (const envType of ctx.environmentTypes) {
      const project = envType.projects.find(p => p.id === projectId);
      if (project && project.allowedActions.includes(action)) {
        return true;
      }
    }

    return false;
  }

  /**
   * ✅ Retourne les actions autorisées pour un projet
   */
  getProjectActions(projectId: number): ActionType[] {
    const ctx = this.context$.value;
    if (!ctx) return [];

    if (ctx.user?.admin) {
      return ['CONSULT', 'CREATE', 'UPDATE', 'DELETE'];
    }

    for (const envType of ctx.environmentTypes) {
      const project = envType.projects.find(p => p.id === projectId);
      if (project) {
        return project.allowedActions;
      }
    }

    return [];
  }

  /**
   * 🔄 MAPPING : Backend → Frontend
   */
  private mapBackendToFrontend(response: any): AuthContext {
    // Normaliser les rôles (Set → Array)
    const rawRoles = response?.user?.roles ?? [];
    const rolesArray: string[] = Array.isArray(rawRoles)
      ? rawRoles
      : typeof rawRoles === 'object'
        ? Object.values(rawRoles)
        : [];

    // Mapper les types d'environnement
    const environmentTypes: EnvironmentTypeWithProjects[] =
      (response?.environmentTypes ?? []).map((envType: any) => ({
        id: envType.id,
        code: envType.code,
        libelle: envType.libelle,
        actif: envType.actif,
        allowedActions: envType.allowedActions ?? [],
        projects: (envType.projects ?? []).map((project: any) => ({
          id: project.id,
          code: project.code,
          libelle: project.libelle,
          description: project.description,
          actif: project.actif,
          allowedActions: project.allowedActions ?? []
        }))
      }));

    return {
      user: {
        ...response.user,
        roles: rolesArray
      },
      environmentTypes
    };
  }
}
