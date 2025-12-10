// src/app/auth/models/auth-context.model.ts

export type ActionType = 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';

/**
 * ✅ Permissions utilisateur
 */
export interface UserPermissions {
  userId: number;
  code: string;
  firstName: string;
  lastName: string;
  email: string;
  profilCode: string;
  profilLibelle: string;
  admin: boolean;
  roles: string[];
}

/**
 * ✅ NOUVEAU : Projet avec ses actions autorisées
 */
export interface ProjectWithActions {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  actif: boolean;
  allowedActions: ActionType[];
}

/**
 * ✅ NOUVEAU : Type d'environnement avec ses projets accessibles
 */
export interface EnvironmentTypeWithProjects {
  id: number;
  code: string;
  libelle: string;
  actif: boolean;
  allowedActions: ActionType[];
  projects: ProjectWithActions[];
}

/**
 * ✅ Contexte d'authentification complet
 */
export interface AuthContext {
  user: UserPermissions;
  environmentTypes: EnvironmentTypeWithProjects[];
}
