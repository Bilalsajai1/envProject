export interface User {
  id: number;

  code: string;
  firstName: string;
  lastName: string;
  email: string;

  actif?: boolean;

  profilId?: number;
  profilCode?: string;
  profilLibelle?: string;

  // Pour affichage / debug
  createdAt?: string;
  updatedAt?: string;
}
