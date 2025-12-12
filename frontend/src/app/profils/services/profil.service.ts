import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../config/environment';
import { PaginatedResponse, PaginationRequest } from '../../users/models/user.model';

export interface ProfilDTO {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  admin: boolean;
  actif: boolean;
  nbUsers?: number;
  nbActiveUsers?: number;
}

export interface ProfilCreateUpdateDTO {
  code: string;
  libelle: string;
  description?: string;
  admin?: boolean;
  actif?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ProfilService {

  private readonly baseUrl = `${environment.apiUrl}/profils`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ProfilDTO[]> {
    return this.http.get<ProfilDTO[]>(this.baseUrl);
  }

  getById(id: number): Observable<ProfilDTO> {
    return this.http.get<ProfilDTO>(`${this.baseUrl}/${id}`);
  }

  create(payload: ProfilCreateUpdateDTO): Observable<ProfilDTO> {
    return this.http.post<ProfilDTO>(this.baseUrl, payload);
  }

  update(id: number, payload: ProfilCreateUpdateDTO): Observable<ProfilDTO> {
    return this.http.put<ProfilDTO>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  search(req: PaginationRequest): Observable<PaginatedResponse<ProfilDTO>> {
    return this.http.post<PaginatedResponse<ProfilDTO>>(
      `${this.baseUrl}/search`,
      req
    );
  }
}
