// src/app/environments/environment-list/environment-list.component.ts

import {
  Component, OnInit, ChangeDetectorRef, OnDestroy, ViewChild
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  debounceTime, distinctUntilChanged, Subject, takeUntil, finalize
} from 'rxjs';

import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';

import {
  EnvironmentDTO, ProjectDTO
} from '../models/environment.model';

import { ProjectService, SortDirection } from '../services/project.service';
import { EnvironmentDialogComponent } from '../components/dialogs/environment-dialog/environment-dialog.component';
import { ConfirmDialogComponent } from '../../users/confirm-dialog/confirm-dialog.component';
import { EnvironmentService } from '../services/environment.service';
import { AuthContextService } from '../../auth/services/auth-context.service';

@Component({
  selector: 'app-environment-list',
  standalone: false,
  templateUrl: './environment-list.component.html',
  styleUrls: ['./environment-list.component.scss']
})
export class EnvironmentListComponent implements OnInit, OnDestroy {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  typeCode: string = '';
  projectId!: number;
  project?: ProjectDTO;

  environments: EnvironmentDTO[] = [];
  displayedColumns = ['code', 'libelle', 'description', 'actif', 'actions'];

  // Pagination
  page = 0;
  size = 10;
  pageSizeOptions = [5, 10, 20, 50];
  totalElements = 0;

  // Tri
  sortField = 'id';
  sortDirection: SortDirection = 'asc';

  searchTerm = '';
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  loading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private envService: EnvironmentService,
    private projectService: ProjectService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private authContext: AuthContextService
  ) {}

  ngOnInit(): void {
    this.initSearchListener();

    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));

    // typeCode depuis route parent
    let r = this.route.parent;
    while (r) {
      const tc = r.snapshot.paramMap.get('typeCode');
      if (tc) {
        this.typeCode = tc.toUpperCase();
        break;
      }
      r = r.parent;
    }

    this.loadProject();
    this.loadEnvironments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ============================================
  // PERMISSION CHECKS
  // ============================================
  canCreateEnvironment(): boolean {
    return this.authContext.canAccessProject(this.projectId, 'CREATE');
  }

  canUpdateEnvironment(): boolean {
    return this.authContext.canAccessProject(this.projectId, 'UPDATE');
  }

  canDeleteEnvironment(): boolean {
    return this.authContext.canAccessProject(this.projectId, 'DELETE');
  }

  canConsultEnvironment(): boolean {
    return this.authContext.canAccessProject(this.projectId, 'CONSULT');
  }

  // ============================================
  // SEARCH
  // ============================================
  private initSearchListener(): void {
    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(value => {
        this.searchTerm = value.trim();
        this.page = 0;
        this.loadEnvironments();
      });
  }

  onSearchChange(value: string) {
    this.searchSubject.next(value);
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }

  // ============================================
  // LOAD
  // ============================================
  loadProject() {
    this.projectService.getById(this.projectId).subscribe({
      next: p => {
        this.project = p;
        this.cdr.markForCheck();
      },
      error: () => (this.project = undefined)
    });
  }

  loadEnvironments(): void {
    this.loading = true;
    this.cdr.markForCheck();

    this.envService
      .getByProjectAndType(this.projectId, this.typeCode, this.searchTerm)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (envs) => {
          this.environments = envs;
          this.totalElements = envs.length;
        },
        error: () => {
          this.snackBar.open('❌ Erreur lors du chargement des environnements', 'Fermer', {
            duration: 3000
          });
        }
      });
  }

  onPageChange(e: PageEvent): void {
    this.page = e.pageIndex;
    this.size = e.pageSize;
    this.loadEnvironments();
  }

  onSortChange(sort: Sort): void {
    if (sort.active && sort.direction) {
      this.sortField = sort.active;
      this.sortDirection = sort.direction as SortDirection;
    } else {
      this.sortField = 'id';
      this.sortDirection = 'asc';
    }
    this.loadEnvironments();
  }

  // ============================================
  // ACTIONS
  // ============================================
  openEnvironment(env: EnvironmentDTO) {
    this.router.navigate([env.id, 'applications'], { relativeTo: this.route });
  }

  addEnvironment() {
    const ref = this.dialog.open(EnvironmentDialogComponent, {
      width: '600px',
      data: { projectId: this.projectId, typeCode: this.typeCode }
    });

    ref.afterClosed().subscribe(ok => ok && this.loadEnvironments());
  }

  editEnvironment(env: EnvironmentDTO) {
    const ref = this.dialog.open(EnvironmentDialogComponent, {
      width: '600px',
      data: { environment: env, projectId: this.projectId, typeCode: this.typeCode }
    });

    ref.afterClosed().subscribe(ok => ok && this.loadEnvironments());
  }

  deleteEnvironment(env: EnvironmentDTO) {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Supprimer l'environnement "${env.libelle}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    ref.afterClosed().subscribe(yes => {
      if (yes) {
        this.envService.delete(env.id).subscribe({
          next: () => {
            this.snackBar.open('✅ Environnement supprimé avec succès', 'Fermer', {
              duration: 3000
            });
            this.loadEnvironments();
          },
          error: () => this.snackBar.open('❌ Erreur suppression', 'Fermer', {
            duration: 3000
          })
        });
      }
    });
  }

  goBack() {
    this.router.navigate(['../../'], { relativeTo: this.route });
  }
}
