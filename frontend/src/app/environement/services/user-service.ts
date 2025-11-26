import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../configuration/environement.config';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  private baseUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  /** Liste des users (avec option de recherche, pour AdminUser) */
  getAllUsers(search?: string): Observable<User[]> {
    let params = new HttpParams();
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<User[]>(this.baseUrl, { params });
  }

  /** Alias pour la datatable simple */
  list(): Observable<User[]> {
    return this.getAllUsers();
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  create(dto: Partial<User>): Observable<User> {
    return this.http.post<User>(this.baseUrl, dto);
  }

  update(id: number, dto: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
