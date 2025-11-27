export interface MenuDTO {
  id: number;
  code: string;
  libelle: string;
  icon?: string;
  route?: string;
  children?: MenuDTO[];
}

export interface AuthContext {
  userId: number;
  username: string;
  email: string;

  profilCode: string;
  profilLibelle: string;
  admin: boolean;

  roles: string[];
  menus: MenuDTO[];
}
