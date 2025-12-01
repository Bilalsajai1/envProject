// src/app/users/services/user.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  UserDTO,
  ProfilDTO,
  PaginationRequest,
  PaginatedResponse
} from '../models/user.model';
import { environment } from '../../config/environment';
import {tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  search(req: PaginationRequest): Observable<PaginatedResponse<UserDTO>> {
    const url = `${this.baseUrl}/users/search`;
    console.log('🌐 UserService.search()');
    console.log('  URL:', url);
    console.log('  Body:', req);

    return this.http.post<PaginatedResponse<UserDTO>>(url, req).pipe(
      tap(response => console.log('✅ Service: réponse reçue', response)),
      tap({
        error: err => console.error('❌ Service: erreur', err)
      })
    );
  }
  getById(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.baseUrl}/users/${id}`);
  }

  create(payload: {
    code: string;
    firstName: string;
    lastName: string;
    email: string;
    actif?: boolean;
    profilId: number;
  }): Observable<UserDTO> {
    return this.http.post<UserDTO>(`${this.baseUrl}/users`, payload);
  }

  update(id: number, payload: {
    code: string;
    firstName: string;
    lastName: string;
    email: string;
    actif?: boolean;
    profilId: number;
  }): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.baseUrl}/users/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/users/${id}`);
  }

  getProfils(): Observable<ProfilDTO[]> {
    return this.http.get<ProfilDTO[]>(`${this.baseUrl}/profils`);
  }
}
