// src/app/users/components/user-list/user-list.component.ts

import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../../services/user.service';
import { PaginatedResponse, UserDTO } from '../../models/user.model';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import {ConfirmDialogComponent} from '../../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-user-list',
  standalone: false,
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

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

  // Pagination
  page = 0;
  size = 10;
  totalElements = 0;
  sortField = 'id';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Recherche
  searchTerm = '';
  private searchSubject = new Subject<string>();

  loading = false;

  constructor(
    private userService: UserService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    // Debounce search pour éviter trop de requêtes
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(term => {
      this.searchTerm = term;
      this.page = 0;
      this.loadUsers();
    });
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    console.log('🔄 === DÉBUT loadUsers() ===');
    console.log('📊 Paramètres:', {
      page: this.page,
      size: this.size,
      sortField: this.sortField,
      sortDirection: this.sortDirection,
      searchTerm: this.searchTerm
    });

    this.loading = true;
    console.log('⏳ Loading = true');

    const filters: any = {};
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      filters.search = this.searchTerm.trim();
    }

    console.log('🔎 Filters:', filters);

    const requestBody = {
      page: this.page,
      size: this.size,
      sortField: this.sortField,
      sortDirection: this.sortDirection,
      filters
    };

    console.log('📤 Requête envoyée:', requestBody);

    this.userService.search(requestBody).subscribe({
      next: (res: PaginatedResponse<UserDTO>) => {
        console.log('✅ Réponse reçue:', res);
        console.log('👥 Nombre d\'utilisateurs:', res.content.length);
        this.users = res.content;
        this.totalElements = res.totalElements;
        this.page = res.page;
        this.size = res.size;
        this.loading = false;
        console.log('✅ Loading = false');
      },
      error: (err) => {
        console.error('❌ ERREUR COMPLÈTE:', err);
        console.error('❌ Status:', err.status);
        console.error('❌ Message:', err.message);
        console.error('❌ Error object:', err.error);

        this.snackBar.open('❌ Erreur lors du chargement', 'Fermer', { duration: 3000 });
        this.loading = false;
        console.log('❌ Loading = false (après erreur)');
      },
      complete: () => {
        console.log('🏁 Observable complété');
      }
    });

    console.log('🔄 === FIN loadUsers() (requête lancée) ===');
  }

  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadUsers();
  }

  onSortChange(sort: Sort): void {
    if (sort.active && sort.direction) {
      this.sortField = sort.active;
      this.sortDirection = sort.direction as 'asc' | 'desc';
    } else {
      this.sortField = 'id';
      this.sortDirection = 'asc';
    }
    this.loadUsers();
  }

  addUser(): void {
    this.router.navigate(['/admin/users/new']);
  }

  editUser(user: UserDTO): void {
    this.router.navigate(['/admin/users', user.id, 'edit']);
  }

  deleteUser(user: UserDTO): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Voulez-vous vraiment supprimer l'utilisateur "${user.firstName} ${user.lastName}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.userService.delete(user.id).subscribe({
          next: () => {
            this.snackBar.open('✅ Utilisateur supprimé avec succès', 'Fermer', {
              duration: 3000,
              panelClass: ['success-snackbar']
            });
            this.loadUsers();
          },
          error: () => {
            this.snackBar.open('❌ Erreur lors de la suppression', 'Fermer', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          }
        });
      }
    });
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }
}
