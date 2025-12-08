export interface UserDTO {
  id: number;
  code: string;
  firstName: string;
  lastName: string;
  email: string;
  actif: boolean;
  profilId: number;
  profilLibelle?: string;
}

export interface UserCreateUpdateDTO {
  code: string;
  firstName: string;
  lastName: string;
  email: string;
  actif?: boolean;
  profilId: number;
  password?: string; // Requis en création, optionnel en modification
}

export interface ProfilDTO {
  id: number;
  code: string;
  libelle: string;
}

export interface PaginationRequest {
  page: number;
  size: number;
  sortField: string;
  sortDirection: 'asc' | 'desc';
  filters: Record<string, any>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
