import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../configuration/environement.config';
import { MenuAdmin } from '../models/menu-admin.model';
import { Role } from '../models/role.model';

@Injectable({
  providedIn: 'root'
})
export class MenuAdminService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // --- CRUD MENUS ---

  getAllMenus(): Observable<MenuAdmin[]> {
    return this.http.get<MenuAdmin[]>(`${this.apiUrl}/menus`);
  }

  getMenu(id: number): Observable<MenuAdmin> {
    return this.http.get<MenuAdmin>(`${this.apiUrl}/menus/${id}`);
  }

  createMenu(payload: Partial<MenuAdmin>): Observable<MenuAdmin> {
    return this.http.post<MenuAdmin>(`${this.apiUrl}/menus`, payload);
  }

  updateMenu(id: number, payload: Partial<MenuAdmin>): Observable<MenuAdmin> {
    return this.http.put<MenuAdmin>(`${this.apiUrl}/menus/${id}`, payload);
  }

  deleteMenu(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/menus/${id}`);
  }

  // --- RÔLES DU MENU ---

  getRolesForMenu(menuId: number): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/menus/${menuId}/roles`);
  }

  updateRolesForMenu(menuId: number, roleIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/menus/${menuId}/roles`, roleIds);
  }
}
