export type ActionType = 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';

export interface EnvTypePermission {
  typeCode: string;
  typeLibelle: string;
  allowedActions: ActionType[];
}

export interface ProjectPermission {
  projectId: number;
  projectCode: string;
  projectLibelle: string;
  actions: ActionType[];
  environmentTypeCodes: string[];
}

export interface ProfilPermissions {
  profilId: number;
  profilCode: string;
  profilLibelle: string;
  isAdmin?: boolean;
  envTypePermissions: EnvTypePermission[];
  projectPermissions: ProjectPermission[];
}

export interface EnvTypePermissionUpdate {
  envTypeCode: string;
  actions: ActionType[];
}

export interface ProjectPermissionUpdate {
  projectId: number;
  actions: ActionType[];
}

export interface SavePermissionsRequest {
  profilId: number;
  envTypePermissions: EnvTypePermissionUpdate[];
  projectPermissions: ProjectPermissionUpdate[];
}

export interface ProfilSimple {
  id: number;
  code: string;
  libelle: string;
}
