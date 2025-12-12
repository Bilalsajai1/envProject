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
  roles: string[];
}

export interface ProjectWithActions {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  actif: boolean;
  allowedActions: ActionType[];
}

export interface EnvironmentTypeWithProjects {
  id: number;
  code: string;
  libelle: string;
  actif: boolean;
  allowedActions: ActionType[];
  projects: ProjectWithActions[];
}

export interface AuthContext {
  user: UserPermissions;
  environmentTypes: EnvironmentTypeWithProjects[];
}
