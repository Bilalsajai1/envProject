import { Injectable } from '@angular/core';
import {environment} from '../../../configuration/environement.config';
import {HttpClient} from '@angular/common/http';
import {Projet} from '../models/projet.model';
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ProjetService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Utilisé pour la navigation par type
  getProjectsByEnvironmentType(typeCode: string): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.apiUrl}/projects/by-environment-type/${typeCode}`);
  }

  // --- CRUD global ---

  getAllProjects(): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.apiUrl}/projects`);
  }

  getProject(id: number): Observable<Projet> {
    return this.http.get<Projet>(`${this.apiUrl}/projects/${id}`);
  }

  createProject(payload: Partial<Projet>): Observable<Projet> {
    return this.http.post<Projet>(`${this.apiUrl}/projects`, payload);
  }

  updateProject(id: number, payload: Partial<Projet>): Observable<Projet> {
    return this.http.put<Projet>(`${this.apiUrl}/projects/${id}`, payload);
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/projects/${id}`);
  }
}
