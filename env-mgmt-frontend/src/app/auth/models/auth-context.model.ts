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
  roles: Set<string>; // ✅ IMPORTANT : Set au lieu de string[]
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
