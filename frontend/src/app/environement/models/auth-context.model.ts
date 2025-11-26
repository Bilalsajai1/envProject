export interface MenuDTO {
  id: number;
  code: string;
  libelle: string;
  route: string;
  icon: string;
  ordre: number;
}

export interface AuthContext {
  userId: number;
  username: string;
  email: string;

  profilCode: string;
  profilLibelle: string;
  admin: boolean;  // ✅ AJOUTER CETTE LIGNE

  roles: string[];
  menus: MenuDTO[];
}
