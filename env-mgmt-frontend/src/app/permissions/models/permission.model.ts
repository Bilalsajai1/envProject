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

// src/app/permissions/models/permission.model.ts

export interface ProfilPermissions {
  profilId: number;
  profilCode: string;
  profilLibelle: string;

  // ✅ Toujours un tableau (jamais undefined)
  envTypePermissions: EnvTypePermission[];

  // ✅ CHANGÉ : retiré le ? pour le rendre obligatoire
  projectPermissions: ProjectPermission[];  // plus de ?:

  // Champs obsolètes (pour compatibilité)
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
