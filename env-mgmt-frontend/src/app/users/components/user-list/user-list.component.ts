import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PageEvent } from '@angular/material/paginator';
import { UserService } from '../../services/user.service';
import { PaginatedResponse, UserDTO } from '../../models/user.model';
import {AuthenticationService} from '../../../auth/services/authentication.service';

@Component({
  selector: 'app-user-list',
  standalone: false,
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {

  users: UserDTO[] = [];
  displayedColumns: string[] = [
    'code',
    'firstName',
    'lastName',
    'email',
    'profilLibelle',
    'actif',
    'actions'
  ];

  page = 0;
  size = 10;
  totalElements = 0;
  sortField = 'id';
  sortDirection: 'asc' | 'desc' = 'asc';

  // 🔍 une seule barre de recherche
  searchTerm = '';

  loading = false;

  constructor(
    private userService: UserService,
    private router: Router ,
    private auth: AuthenticationService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;

    const filters: any = {};

    // filtre global
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      filters.search = this.searchTerm.trim();
    }

    this.userService.search({
      page: this.page,
      size: this.size,
      sortField: this.sortField,
      sortDirection: this.sortDirection,
      filters
    }).subscribe({
      next: (res: PaginatedResponse<UserDTO>) => {
        this.users = res.content;
        this.totalElements = res.totalElements;
        this.page = res.page;
        this.size = res.size;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
  canEdit(): boolean {
    return this.auth.hasRole('ROLE_USERS_EDIT');
  }

  canDelete(): boolean {
    return this.auth.hasRole('ROLE_USERS_DELETE');
  }
  // ⚡ instantané : chaque frappe ⇒ requête
  onSearchChange(value: string): void {
    this.searchTerm = value;
    this.page = 0;
    this.loadUsers();
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadUsers();
  }

  changeSort(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.loadUsers();
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) {
      return 'unfold_more';
    }
    return this.sortDirection === 'asc' ? 'arrow_upward' : 'arrow_downward';
  }

  addUser(): void {
    this.router.navigate(['/admin/users/new']);
  }

  editUser(user: UserDTO): void {
    this.router.navigate(['/admin/users', user.id, 'edit']);
  }

  deleteUser(user: UserDTO): void {
    if (!confirm(`Supprimer l'utilisateur ${user.code} ?`)) {
      return;
    }
    this.userService.delete(user.id).subscribe({
      next: () => this.loadUsers()
    });
  }
}
