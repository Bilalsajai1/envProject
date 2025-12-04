export type ActionType = 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';

export interface EnvTypePermission {
  typeCode: string;
  typeLibelle: string;
  actions: ActionType[];
}

export interface ProjectPermission {
  projectId: number;
  projectCode: string;
  projectLibelle: string;
  actions: ActionType[];
}


export interface ProfilPermissions {
  profilId: number;
  profilCode: string;
  profilLibelle: string;

  envTypePermissions: EnvTypePermission[];


  projectPermissions: ProjectPermission[];  // plus de ?:


  projectActions?: ActionType[];
  environmentActions?: ActionType[];
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
  envTypePermissions: { [typeCode: string]: ActionType[] };
  projectActions?: ActionType[];
  environmentActions?: ActionType[];
}

export interface ProfilSimple {
  id: number;
  code: string;
  libelle: string;
}
