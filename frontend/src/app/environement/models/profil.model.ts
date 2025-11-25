export interface Profil {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  admin?: boolean;
  actif?: boolean;
}
