export type ActionType = 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';

export interface EnvTypePermission {
  typeCode: string;
  typeLibelle: string;
  actions: ActionType[];
}

/** 🔥 Nouveau : permissions par projet */
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

  // Toujours présent
  envTypePermissions: EnvTypePermission[];

  // 🔥 Nouveau : liste des projets avec leurs actions
  projectPermissions?: ProjectPermission[];

  // On laisse ces champs pour compatibilité template éventuelle (mais on ne les utilise plus)
  projectActions?: ActionType[];
  environmentActions?: ActionType[];
}

/** DTO d’update vers le backend */
export interface EnvTypePermissionUpdate {
  envTypeCode: string;
  actions: ActionType[];
}

export interface ProjectPermissionUpdate {
  projectId: number;
  actions: ActionType[];
}

/** Déjà présent */
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
