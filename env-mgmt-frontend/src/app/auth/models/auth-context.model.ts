// src/app/auth/models/auth-context.model.ts
export type ActionType = 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';

export interface UserPermissions {
  userId: number;
  code: string;
  firstName: string;
  lastName: string;
  email: string;
  profilCode: string;
  profilLibelle: string;
  admin: boolean;
  roles: string[];   // ✅ tableau de rôles
}

export interface EnvironmentTypePermission {
  id: number;
  code: string;
  libelle: string;
  actif: boolean;
  allowedActions: ActionType[];
}

export interface AuthContext {
  user: UserPermissions;
  environmentTypes: EnvironmentTypePermission[];
}
