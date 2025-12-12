import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  Subject,
  distinctUntilChanged,
  takeUntil,
  finalize
} from 'rxjs';

import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';

import { ProjectDTO } from '../models/environment.model';
import {
  PaginatedResponse,
  PaginationRequest,
  ProjectService,
  SortDirection
} from '../services/project.service';
import { ProjectDialogComponent } from '../components/dialogs/project-dialog/project-dialog.component';
import { ConfirmDialogComponent } from '../../users/confirm-dialog/confirm-dialog.component';
import { AuthContextService } from '../../auth/services/auth-context.service';

@Component({
  selector: 'app-project-list',
  standalone: false,
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectListComponent implements OnInit, OnDestroy {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  typeCode: string = '';

  projects: ProjectDTO[] = [];
  displayedColumns: string[] = ['code', 'libelle', 'description', 'actif', 'actions'];


  page = 0;
  size = 10;
  readonly pageSizeOptions: number[] = [5, 10, 20, 50];
  totalElements = 0;

  sortField = 'id';
  sortDirection: SortDirection = 'asc';

  searchTerm = '';
  private readonly searchSubject = new Subject<string>();

  loading = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly projectService: ProjectService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly cdr: ChangeDetectorRef,
    private readonly authContext: AuthContextService
  ) {}

  ngOnInit(): void {
    this.initSearchListener();

    this.route.parent?.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.typeCode = params.get('typeCode')?.toUpperCase() || '';
        this.page = 0;
        this.loadProjects();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  canCreateProject(): boolean {
    const ctx = this.authContext.getCurrentContext();
    if (ctx?.user?.admin) return true;
    const type = ctx?.environmentTypes.find(t => t.code?.toUpperCase() === this.typeCode?.toUpperCase());
    return type?.allowedActions?.includes('CREATE') ?? false;
  }

  canUpdateProject(projectId: number): boolean {
    return this.authContext.canAccessProject(projectId, 'UPDATE');
  }

  canDeleteProject(projectId: number): boolean {
    return this.authContext.canAccessProject(projectId, 'DELETE');
  }

  canConsultProject(projectId: number): boolean {
    return this.authContext.canAccessProject(projectId, 'CONSULT');
  }

  private initSearchListener(): void {
    this.searchSubject
      .pipe(
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(term => {
        this.searchTerm = term.trim();
        this.page = 0;
        this.loadProjects();
      });
  }

  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }

  private buildFilters(): Record<string, any> {
    const filters: Record<string, any> = {};

    if (this.searchTerm) {
      filters['search'] = this.searchTerm.trim();
    }

    if (this.typeCode) {
      filters['typeCode'] = this.typeCode;
    }

    return filters;
  }


  loadProjects(): void {
    if (!this.typeCode) {
      return;
    }

    const requestBody: PaginationRequest = {
      page: this.page,
      size: this.size,
      sortField: this.sortField,
      sortDirection: this.sortDirection,
      filters: this.buildFilters()
    };

    this.loading = true;
    this.cdr.markForCheck();

    this.projectService
      .search(requestBody)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (res: PaginatedResponse<ProjectDTO>) => {
          this.projects = res.content ?? [];
          this.totalElements = res.totalElements ?? 0;
          this.page = res.page ?? 0;
          this.size = res.size ?? this.size;
        },
        error: (err) => {
          this.snackBar.open('❌ Erreur lors du chargement des projets', 'Fermer', {
            duration: 3000
          });
        }
      });
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadProjects();
  }

  onSortChange(sort: Sort): void {
    if (sort.active && sort.direction) {
      this.sortField = sort.active;
      this.sortDirection = sort.direction as SortDirection;
    } else {
      this.sortField = 'id';
      this.sortDirection = 'asc';
    }
    this.loadProjects();
  }

  openProject(project: ProjectDTO): void {
    this.router.navigate([project.id, 'environments'], {
      relativeTo: this.route
    });
  }

  addProject(): void {
    const dialogRef = this.dialog.open(ProjectDialogComponent, {
      width: '600px',
      data: { typeCode: this.typeCode }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProjects();
      }
    });
  }

  editProject(project: ProjectDTO): void {
    const dialogRef = this.dialog.open(ProjectDialogComponent, {
      width: '600px',
      data: { project, typeCode: this.typeCode }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProjects();
      }
    });
  }

  deleteProject(project: ProjectDTO): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Voulez-vous vraiment supprimer le projet "${project.libelle}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.projectService.delete(project.id).subscribe({
          next: () => {
            this.snackBar.open('✅ Projet supprimé avec succès', 'Fermer', {
              duration: 3000
            });
            this.loadProjects();
          },
          error: () => {
            this.snackBar.open('❌ Erreur lors de la suppression', 'Fermer', {
              duration: 3000
            });
          }
        });
      }
    });
  }
}
