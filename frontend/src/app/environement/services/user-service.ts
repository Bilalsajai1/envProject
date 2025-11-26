import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../configuration/environement.config';
import { User } from '../models/user.model';

export interface UserSearchRequest {
  page: number;
  size: number;
  sort?: string;

  text?: string;
  code?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  actif?: boolean | null;
  profilId?: number | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  searchUsers(req: UserSearchRequest): Observable<PageResponse<User>> {
    return this.http.post<PageResponse<User>>(
      `${this.apiUrl}/search`,
      req
    );
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  createUser(payload: Partial<User>): Observable<User> {
    return this.http.post<User>(this.apiUrl, payload);
  }

  updateUser(id: number, payload: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, payload);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
