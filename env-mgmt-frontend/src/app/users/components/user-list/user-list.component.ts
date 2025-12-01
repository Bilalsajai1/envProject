// src/app/users/components/user-list/user-list.component.ts

import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ChangeDetectionStrategy,
  ChangeDetectorRef
} from '@angular/core';
import { Router } from '@angular/router';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  debounceTime,
  distinctUntilChanged,
  Subject,
  takeUntil,
  finalize
} from 'rxjs';

import { UserService } from '../../services/user.service';
import { PaginatedResponse, UserDTO } from '../../models/user.model';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog.component';

type SortDirection = 'asc' | 'desc';

interface UserSearchRequest {
  page: number;
  size: number;
  sortField: string;
  sortDirection: SortDirection;
  filters: Record<string, any>;
}

@Component({
  selector: 'app-user-list',
  standalone: false,
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserListComponent implements OnInit, OnDestroy {

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

  page = 0;
  size = 10;
  readonly pageSizeOptions: number[] = [5, 10, 20, 50];

  totalElements = 0;
  sortField = 'id';
  sortDirection: SortDirection = 'asc';

  searchTerm = '';
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  loading = false;

  constructor(
    private readonly userService: UserService,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly cdr: ChangeDetectorRef
  ) {}

  // -----------------------
  // Lifecycle
  // -----------------------

  ngOnInit(): void {
    this.initSearchListener();
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // -----------------------
  // Initialisation
  // -----------------------

  private initSearchListener(): void {
    this.searchSubject
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(term => {
        this.searchTerm = term.trim();
        this.page = 0; // on revient à la première page
        this.loadUsers();
      });
  }

  // -----------------------
  // Chargement des données
  // -----------------------

  private buildFilters(): Record<string, any> {
    const filters: Record<string, any> = {};
    if (this.searchTerm) {
      filters['search'] = this.searchTerm.trim();
    }
    return filters;
  }

  loadUsers(): void {
    const requestBody: UserSearchRequest = {
      page: this.page,
      size: this.size,
      sortField: this.sortField,
      sortDirection: this.sortDirection,
      filters: this.buildFilters()
    };

    this.loading = true;

    this.userService
      .search(requestBody)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (res: PaginatedResponse<UserDTO>) => {
          this.users = res.content ?? [];
          this.totalElements = res.totalElements ?? 0;
          this.page = res.page ?? 0;
          this.size = res.size ?? this.size;
        },
        error: () => {
          this.showSnackBar(
            '❌ Erreur lors du chargement des utilisateurs',
            'error-snackbar'
          );
        }
      });
  }

  refresh(): void {
    this.loadUsers();
  }

  // -----------------------
  // Événements UI
  // -----------------------

  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadUsers();
  }

  onSortChange(sort: Sort): void {
    if (sort.active && sort.direction) {
      this.sortField = sort.active;
      this.sortDirection = sort.direction as SortDirection;
    } else {
      this.sortField = 'id';
      this.sortDirection = 'asc';
    }
    this.loadUsers();
  }

  // -----------------------
  // Actions
  // -----------------------

  addUser(): void {
    this.router.navigate(['/admin/users/new']);
  }

  editUser(user: UserDTO): void {
    if (!user.id) return;
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
      if (!confirmed || !user.id) return;

      this.userService.delete(user.id).subscribe({
        next: () => {
          this.showSnackBar(
            '✅ Utilisateur supprimé avec succès',
            'success-snackbar'
          );
          this.loadUsers();
        },
        error: () => {
          this.showSnackBar(
            '❌ Erreur lors de la suppression',
            'error-snackbar'
          );
        }
      });
    });
  }

  // -----------------------
  // Helpers
  // -----------------------

  get isEmpty(): boolean {
    return !this.loading && this.users.length === 0;
  }

  get resultsLabel(): string {
    const count = this.totalElements || 0;
    if (count === 0) {
      return 'Aucun utilisateur';
    }
    if (count === 1) {
      return '1 utilisateur';
    }
    return `${count} utilisateurs`;
  }

  private showSnackBar(message: string, panelClass: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: [panelClass]
    });
  }
}
