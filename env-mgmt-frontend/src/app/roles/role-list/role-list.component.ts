// src/app/roles/components/role-list/role-list.component.ts

import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { FormControl } from '@angular/forms';
import {RoleDTO, RoleSearchRequest, RoleService} from '../services/role.service';
import {ConfirmDialogComponent} from '../../users/confirm-dialog/confirm-dialog.component';


@Component({
  selector: 'app-role-list',
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss'],
  standalone: false
})
export class RoleListComponent implements OnInit, OnDestroy {

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  roles: RoleDTO[] = [];
  loading = false;
  exporting = false;

  // Pagination
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  pageSizeOptions = [5, 10, 25, 50, 100];

  // Recherche et filtres
  searchControl = new FormControl('');
  statusFilter: 'all' | 'active' | 'inactive' = 'all';

  displayedColumns: string[] = [
    'code',
    'libelle',
    'action',
    'menu',
    'environnement',
    'projet',
    'actif',
    'actions'
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private roleService: RoleService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.setupSearch();
    this.loadRoles();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadRoles();
      });
  }

  loadRoles(): void {
    this.loading = true;

    const request: RoleSearchRequest = {
      page: this.pageIndex,
      size: this.pageSize,
      search: this.searchControl.value || undefined,
      actif: this.getActifFilter()
    };

    this.roleService.search(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.roles = response.content;
          this.totalElements = response.totalElements;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des rôles:', error);
          this.showError('Impossible de charger les rôles');
          this.loading = false;
        }
      });
  }

  private getActifFilter(): boolean | undefined {
    if (this.statusFilter === 'active') return true;
    if (this.statusFilter === 'inactive') return false;
    return undefined;
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRoles();
  }

  onStatusFilterChange(status: 'all' | 'active' | 'inactive'): void {
    this.statusFilter = status;
    this.pageIndex = 0;
    this.loadRoles();
  }

  clearSearch(): void {
    this.searchControl.setValue('');
    this.statusFilter = 'all';
    this.pageIndex = 0;
    this.loadRoles();
  }

  add(): void {
    this.router.navigate(['/admin/roles/new']);
  }

  edit(role: RoleDTO): void {
    this.router.navigate(['/admin/roles', role.id, 'edit']);
  }

  view(role: RoleDTO): void {
    this.router.navigate(['/admin/roles', role.id]);
  }

  delete(role: RoleDTO): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '450px',
      data: {
        title: 'Supprimer le rôle',
        message: `Êtes-vous sûr de vouloir supprimer le rôle "${role.libelle}" (${role.code}) ? Cette action est irréversible.`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler',
        type: 'danger',
        icon: 'delete_forever'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.performDelete(role.id);
      }
    });
  }

  private performDelete(id: number): void {
    this.roleService.delete(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.showSuccess('Rôle supprimé avec succès');
          this.loadRoles();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression:', error);
          this.showError('Impossible de supprimer le rôle');
        }
      });
  }

  toggleStatus(role: RoleDTO, event: Event): void {
    event.stopPropagation();

    const newStatus = !role.actif;
    const action = newStatus ? 'activer' : 'désactiver';

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '450px',
      data: {
        title: `${action.charAt(0).toUpperCase() + action.slice(1)} le rôle`,
        message: `Voulez-vous ${action} le rôle "${role.libelle}" ?`,
        confirmText: action.charAt(0).toUpperCase() + action.slice(1),
        type: 'warning'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.performToggleStatus(role, newStatus);
      }
    });
  }

  private performToggleStatus(role: RoleDTO, newStatus: boolean): void {
    const payload = {
      code: role.code,
      libelle: role.libelle,
      action: role.action,
      actif: newStatus,
      menuId: role.menuId,
      environnementId: role.environnementId,
      projetId: role.projetId
    };

    this.roleService.update(role.id, payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const message = newStatus ? 'Rôle activé' : 'Rôle désactivé';
          this.showSuccess(message);
          this.loadRoles();
        },
        error: (error) => {
          console.error('Erreur lors du changement de statut:', error);
          this.showError('Impossible de modifier le statut');
        }
      });
  }

  exportExcel(): void {
    this.exporting = true;

    const request: RoleSearchRequest = {
      page: 0,
      size: this.totalElements || 1000,
      search: this.searchControl.value || undefined,
      actif: this.getActifFilter()
    };

    this.roleService.exportToExcel(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `roles_${new Date().toISOString().split('T')[0]}.xlsx`;
          link.click();
          window.URL.revokeObjectURL(url);
          this.exporting = false;
          this.showSuccess('Export Excel réussi');
        },
        error: (error) => {
          console.error('Erreur lors de l\'export:', error);
          this.showError('Impossible d\'exporter les données');
          this.exporting = false;
        }
      });
  }

  refresh(): void {
    this.loadRoles();
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  get filteredRolesCount(): number {
    return this.roles.length;
  }

  get hasFilters(): boolean {
    return !!this.searchControl.value || this.statusFilter !== 'all';
  }
}
