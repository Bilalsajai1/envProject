export type ActionType = 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';

export interface EnvTypePermission {
  typeCode: string;
  typeLibelle: string;
  actions: ActionType[];
}

export interface ProfilPermissions {
  profilId: number;
  profilCode: string;
  profilLibelle: string;
  envTypePermissions: EnvTypePermission[];
  projectActions: ActionType[];
  environmentActions: ActionType[];
}

export interface SavePermissionsRequest {
  profilId: number;
  envTypePermissions: { [typeCode: string]: ActionType[] };
  projectActions: ActionType[];
  environmentActions: ActionType[];
}

export interface ProfilSimple {
  id: number;
  code: string;
  libelle: string;
}
