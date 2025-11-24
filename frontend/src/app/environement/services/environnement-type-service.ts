import { Injectable } from '@angular/core';
import {environment} from '../../../configuration/environement.config';
import {HttpClient} from '@angular/common/http';
import {EnvironmentType} from '../models/environment-type.model';
import {Observable} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class EnvironmentTypeService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllTypes(): Observable<EnvironmentType[]> {
    return this.http.get<EnvironmentType[]>(`${this.apiUrl}/environment-types`);
  }

  getActiveTypes(): Observable<EnvironmentType[]> {
    return this.http.get<EnvironmentType[]>(`${this.apiUrl}/environment-types/actives`);
  }

  create(dto: Partial<EnvironmentType>): Observable<EnvironmentType> {
    return this.http.post<EnvironmentType>(`${this.apiUrl}/environment-types`, dto);
  }

  update(id: number, dto: Partial<EnvironmentType>): Observable<EnvironmentType> {
    return this.http.put<EnvironmentType>(`${this.apiUrl}/environment-types/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/environment-types/${id}`);
  }
}
