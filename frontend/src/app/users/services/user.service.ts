import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  UserDTO,
  UserCreateUpdateDTO,
  ProfilDTO,
  PaginationRequest,
  PaginatedResponse
} from '../models/user.model';
import { environment } from '../../config/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  search(req: PaginationRequest): Observable<PaginatedResponse<UserDTO>> {
    return this.http.post<PaginatedResponse<UserDTO>>(`${this.baseUrl}/users/search`, req);
  }

  getById(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.baseUrl}/users/${id}`);
  }

  create(payload: UserCreateUpdateDTO): Observable<UserDTO> {
    return this.http.post<UserDTO>(`${this.baseUrl}/users`, payload);
  }

  update(id: number, payload: UserCreateUpdateDTO): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.baseUrl}/users/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/users/${id}`);
  }

  getProfils(): Observable<ProfilDTO[]> {
    return this.http.get<ProfilDTO[]>(`${this.baseUrl}/profils`);
  }
}
