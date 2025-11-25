import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../configuration/environement.config';
import { Profil } from '../models/profil.model';

@Injectable({
  providedIn: 'root'
})
export class ProfilService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Profil[]> {
    return this.http.get<Profil[]>(`${this.apiUrl}/profils`);
  }

  get(id: number): Observable<Profil> {
    return this.http.get<Profil>(`${this.apiUrl}/profils/${id}`);
  }

  create(payload: Partial<Profil>): Observable<Profil> {
    return this.http.post<Profil>(`${this.apiUrl}/profils`, payload);
  }

  update(id: number, payload: Partial<Profil>): Observable<Profil> {
    return this.http.put<Profil>(`${this.apiUrl}/profils/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/profils/${id}`);
  }

  /** Renvoie normalement la liste des IDs de rôles du profil */
  getRoleIdsForProfil(profilId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/profils/${profilId}/roles`);
  }

  /** Met à jour les rôles du profil (body = [1,2,3]) */
  updateRoles(profilId: number, roleIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/profils/${profilId}/roles`, roleIds);
  }
}
