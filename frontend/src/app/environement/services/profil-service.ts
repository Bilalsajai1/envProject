import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../configuration/environement.config';
import { Profil } from '../models/profil.model';

@Injectable({
  providedIn: 'root'
})
export class ProfilService {

  private apiUrl = `${environment.apiUrl}/profils`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Profil[]> {
    return this.http.get<Profil[]>(this.apiUrl);
  }

  getById(id: number): Observable<Profil> {
    return this.http.get<Profil>(`${this.apiUrl}/${id}`);
  }

  create(payload: Partial<Profil>): Observable<Profil> {
    return this.http.post<Profil>(this.apiUrl, payload);
  }

  update(id: number, payload: Partial<Profil>): Observable<Profil> {
    return this.http.put<Profil>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // roles d’un profil -> liste d’IDs
  getRolesByProfil(profilId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${profilId}/roles`);
  }

  // mise à jour des roles pour un profil
  updateProfilRoles(profilId: number, roleIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${profilId}/roles`, roleIds);
  }
}
