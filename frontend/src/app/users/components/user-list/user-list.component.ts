import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ChangeDetectionStrategy,
  ChangeDetectorRef
} from '@angular/core';
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
import {UserDTO } from '../../models/user.model';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog.component';
import { UserFormComponent } from '../user-form/user-form.component';

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
  totalElements = 0;

  displayedColumns = [
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
  readonly pageSizeOptions = [5, 10, 20, 50];

  searchTerm = '';
  loading = false;

  sortField = 'id';
  sortDirection: SortDirection = 'asc';

  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly userService: UserService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.setupSearchListener();
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupSearchListener(): void {
    this.searchSubject
      .pipe(
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.page = 0;
        this.loadUsers();
      });
  }

  private buildFilters(): Record<string, any> {
    return this.searchTerm.trim()
      ? { search: this.searchTerm.trim() }
      : {};
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
        next: res => {
          this.users = res.content ?? [];
          this.totalElements = res.totalElements ?? 0;
        },
        error: () => {
          this.showSnackBar('Erreur lors du chargement des utilisateurs', 'error-snackbar');
        }
      });
  }

  refresh(): void {
    this.loadUsers();
  }

  onSearchChange(value: string): void {
    this.searchTerm = value;
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
    this.sortField = sort.active || 'id';
    this.sortDirection = (sort.direction as SortDirection) || 'asc';
    this.loadUsers();
  }

  addUser(): void {
    this.openUserForm('create');
  }

  editUser(user: UserDTO): void {
    if (user.id) this.openUserForm('edit', user.id);
  }

  private openUserForm(mode: 'create' | 'edit', userId?: number): void {
    const dialogRef = this.dialog.open(UserFormComponent, {
      width: '900px',
      maxWidth: '100vw',
      maxHeight: '90vh',
      disableClose: true,
      panelClass: 'user-form-dialog',
      data: { mode, userId }
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe(shouldReload => {
        if (shouldReload) this.loadUsers();
      });
  }

  deleteUser(user: UserDTO): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Supprimer l'utilisateur "${user.firstName} ${user.lastName}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe(confirmed => {
        if (!confirmed || !user.id) return;

        this.userService.delete(user.id).subscribe({
          next: () => {
            this.showSnackBar('Utilisateur supprimé avec succès', 'success-snackbar');
            this.loadUsers();
          },
          error: () => {
            this.showSnackBar('Erreur lors de la suppression', 'error-snackbar');
          }
        });
      });
  }

  get isEmpty(): boolean {
    return !this.loading && this.users.length === 0;
  }

  trackByUser(_: number, user: UserDTO): number | undefined {
    return user.id;
  }

  get resultsLabel(): string {
    return this.totalElements === 0
      ? 'Aucun utilisateur'
      : this.totalElements === 1
        ? '1 utilisateur'
        : `${this.totalElements} utilisateurs`;
  }

  private showSnackBar(message: string, panelClass: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: [panelClass]
    });
  }
}
