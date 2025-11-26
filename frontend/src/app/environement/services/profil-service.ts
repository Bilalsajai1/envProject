import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Profil } from '../models/profil.model';
import { environment } from '../../../configuration/environement.config';

@Injectable({
  providedIn: 'root'
})
export class ProfilService {

  private apiUrl = `${environment.apiUrl}/profils`;
  private rolesUrl = `${environment.apiUrl}/roles`;

  constructor(private http: HttpClient) {}

  /** Récupérer tous les profils */
  getAll(): Observable<Profil[]> {
    return this.http.get<Profil[]>(this.apiUrl);
  }

  /** Récupérer un profil par ID */
  getById(id: number): Observable<Profil> {
    return this.http.get<Profil>(`${this.apiUrl}/${id}`);
  }

  /** Créer un profil */
  create(payload: Partial<Profil>): Observable<Profil> {
    return this.http.post<Profil>(this.apiUrl, payload);
  }

  /** Mettre à jour un profil */
  update(id: number, payload: Partial<Profil>): Observable<Profil> {
    return this.http.put<Profil>(`${this.apiUrl}/${id}`, payload);
  }

  /** Supprimer un profil */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /** 🔹 Récupérer tous les rôles disponibles (RoleDTO côté backend) */
  getAllRoles(): Observable<any[]> {
    return this.http.get<any[]>(this.rolesUrl);
  }

  /** Récupérer les IDs des rôles d’un profil */
  getRolesByProfil(profilId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${profilId}/roles`);
  }

  /** Mettre à jour les rôles d’un profil */
  updateProfilRoles(profilId: number, roleIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${profilId}/roles`, roleIds);
  }
}
