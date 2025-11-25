export interface Role {
  id: number;
  code: string;
  libelle?: string;
  action?: string; // CONSULT, CREATE, UPDATE, DELETE...
  actif?: boolean;

  menuId?: number;
  menuCode?: string;

  environnementId?: number;
  environnementCode?: string;
}
