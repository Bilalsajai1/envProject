export interface Role {
  id: number;

  code: string;
  libelle: string;
  action: 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';
  actif: boolean;

  menuId?: number | null;
  menuCode?: string | null;

  environnementId?: number | null;
  environnementCode?: string | null;

  createdAt?: string;
  updatedAt?: string;
}
