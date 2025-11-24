export interface Environnement {
  id: number;
  code: string;
  libelle?: string;
  description?: string;
  actif?: boolean;

  projetId?: number;
  typeId?: number;
}
