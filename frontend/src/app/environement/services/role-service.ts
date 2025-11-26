import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../configuration/environement.config';
import { Role } from '../models/role.model';

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private readonly apiUrl = `${environment.apiUrl}/roles`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Role[]> {
    return this.http.get<Role[]>(this.apiUrl);
  }

  getById(id: number): Observable<Role> {
    return this.http.get<Role>(`${this.apiUrl}/${id}`);
  }

  getByMenu(menuId: number): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/by-menu/${menuId}`);
  }

  getByEnv(envId: number): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/by-env/${envId}`);
  }
}
