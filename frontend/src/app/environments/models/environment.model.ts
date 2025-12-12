export interface ProjectDTO {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  actif: boolean;
  envTypeCode?: string;
  envTypeCodes?: string[];
}

export interface EnvironmentDTO {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  actif: boolean;
  projetId: number;
  typeCode: string;
}

export interface ApplicationDTO {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  actif: boolean;
}

export interface EnvApplicationDTO {
  id: number;
  environnementId: number;
  applicationId: number;
  applicationCode: string;
  applicationLibelle: string;
  protocole?: string;
  host?: string;
  port?: number;
  url?: string;
  username?: string;
  password?: string;
  passwordMasked?: string;
  description?: string;
  actif: boolean;
  dateDerniereLivraison?: string;
}

export interface EnvironmentTypeDTO {
  id: number;
  code: string;
  libelle: string;
  actif: boolean;
}
