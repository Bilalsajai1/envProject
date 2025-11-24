import { Injectable } from '@angular/core';
import {environment} from '../../../configuration/environement.config';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Environnement} from '../models/environnement.model';

@Injectable({
  providedIn: 'root'
})
export class EnvironnementService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getEnvironmentsByProjetAndType(
    projetId: number,
    typeCode: string
  ): Observable<Environnement[]> {
    const params = new HttpParams()
      .set('projetId', projetId.toString())
      .set('typeCode', typeCode);

    return this.http.get<Environnement[]>(
      `${this.apiUrl}/environnements`,
      { params }
    );
  }

  createEnvironnement(dto: Partial<Environnement> & { projetId: number; typeId: number }): Observable<Environnement> {
    return this.http.post<Environnement>(
      `${this.apiUrl}/environnements`,
      dto
    );
  }
  updateEnvironnement(id: number, dto: Partial<Environnement>): Observable<Environnement> {
    return this.http.put<Environnement>(
      `${this.apiUrl}/environnements/${id}`,
      dto
    );
  }

  deleteEnvironnement(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/environnements/${id}`
    );
  }
}
