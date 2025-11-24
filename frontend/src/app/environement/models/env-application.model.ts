export interface EnvApplication {
  id: number;

  environnementId?: number;
  applicationId?: number;
  applicationCode?: string;
  applicationLibelle?: string;

  protocole?: string;
  host?: string;
  port?: number;
  url?: string;

  username?: string;

  password?: string;
  passwordMasked?: string;

  description?: string;

  actif?: boolean;
  dateDerniereLivraison?: string;
}
