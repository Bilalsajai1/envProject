
import {
  ChangeDetectorRef,
  Component,
  OnInit,
  OnDestroy,
  ViewChild
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, Sort } from '@angular/material/sort';

  import { Subject,distinctUntilChanged, takeUntil } from 'rxjs';

import { ApplicationService } from '../services/application.service';
import { EnvApplicationDTO } from '../models/environment.model';

import { ConfirmDialogComponent } from '../../users/confirm-dialog/confirm-dialog.component';
import { ApplicationDialogComponent } from '../components/dialogs/application-dialog/application-dialog.component';
import { AuthContextService } from '../../auth/services/auth-context.service';

@Component({
  selector: 'app-application-list',
  standalone: false,
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.scss']
})
export class ApplicationListComponent implements OnInit, OnDestroy {

  @ViewChild(MatSort) sort!: MatSort;

  typeCode: string = '';
  projectId!: number;
  environmentId!: number;

  applications: EnvApplicationDTO[] = [];
  loading = false;

  searchTerm: string = '';
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  displayedColumns = [
    'application',
    'protocole',
    'host',
    'port',
    'username',
    'password',
    'url',
    'dateDerniereLivraison',
    'actions'
  ];
  sortActive: string = 'application';
  sortDirection: 'asc' | 'desc' | '' = 'asc';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private applicationService: ApplicationService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private authContext: AuthContextService
  ) {}

  ngOnInit(): void {
    this.initSearchListener();
    this.initializeComponent();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  canCreateApplication(): boolean {
    return this.authContext.canAccessProject(this.projectId, 'CREATE');
  }

  canUpdateApplication(): boolean {
    return this.authContext.canAccessProject(this.projectId, 'UPDATE');
  }

  canDeleteApplication(): boolean {
    return this.authContext.canAccessProject(this.projectId, 'DELETE');
  }

  private initSearchListener(): void {
    this.searchSubject
      .pipe(
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(term => {
        this.searchTerm = term.trim();
        this.loadApplications();
      });
  }

  onSortChange(sort: Sort): void {
    this.sortActive = sort.active;
    this.sortDirection = (sort.direction as 'asc' | 'desc' | '');
    this.applySorting();
  }

  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }

  private initializeComponent(): void {
    const typeParamRoute =
      this.route.parent?.parent?.parent ||
      this.route.parent?.parent ||
      this.route.parent;

    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));
    this.environmentId = Number(this.route.snapshot.paramMap.get('environmentId'));
    this.typeCode = (typeParamRoute?.snapshot.paramMap.get('typeCode') || '').toUpperCase();

    if (!this.environmentId) {
      this.snackBar.open('Environnement introuvable', 'Fermer', { duration: 3000 });
      return;
    }

    this.loadApplications();
  }

  loadApplications(): void {
    this.loading = true;
    this.cdr.markForCheck();

    this.applicationService
      .getByEnvironment(this.environmentId, this.searchTerm)
      .subscribe({
        next: (apps) => {
          this.applications = (apps ?? []).map(a => ({
            ...a,
            password: undefined
          }));
          this.applySorting();
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 3000 });
          this.loading = false;
          this.cdr.markForCheck();
        }
      });
  }

  addApplication(): void {
    const dialogRef = this.dialog.open(ApplicationDialogComponent, {
      width: '900px',
      data: {
        environmentId: this.environmentId
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadApplications();
    });
  }

  editApplication(app: EnvApplicationDTO): void {
    const dialogRef = this.dialog.open(ApplicationDialogComponent, {
      width: '900px',
      data: {
        application: app,
        environmentId: this.environmentId
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadApplications();
    });
  }

  deleteApplication(app: EnvApplicationDTO): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Supprimer l\'application',
        message: `Voulez-vous vraiment supprimer "${app.applicationLibelle}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.applicationService.delete(app.id).subscribe({
          next: () => {
            this.snackBar.open('Application supprimee avec succes', 'Fermer', {
              duration: 3000
            });
            this.loadApplications();
          },
          error: () => {
            this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
              duration: 3000
            });
          }
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['../../'], { relativeTo: this.route });
  }

  private applySorting(): void {
    if (!this.sortDirection) {
      return;
    }

    const directionFactor = this.sortDirection === 'asc' ? 1 : -1;

    const sorted = [...this.applications].sort((a, b) => {
      const aVal = this.getSortableValue(a, this.sortActive);
      const bVal = this.getSortableValue(b, this.sortActive);

      if (aVal === bVal) return 0;
      if (aVal === undefined || aVal === null) return -1 * directionFactor;
      if (bVal === undefined || bVal === null) return 1 * directionFactor;

      if (typeof aVal === 'number' && typeof bVal === 'number') {
        return (aVal - bVal) * directionFactor;
      }

      return aVal.toString().localeCompare(bVal.toString()) * directionFactor;
    });

    this.applications = sorted;
    this.cdr.markForCheck();
  }

  private getSortableValue(app: EnvApplicationDTO, column: string): any {
    switch (column) {
      case 'application':
        return app.applicationLibelle ?? '';
      case 'protocole':
        return app.protocole ?? '';
      case 'host':
        return app.host ?? '';
      case 'port':
        return app.port ?? 0;
      case 'username':
        return app.username ?? '';
      case 'password':
        return app.passwordMasked ?? '';
      case 'url':
        return app.url ?? '';
      case 'dateDerniereLivraison':
        return app.dateDerniereLivraison ? new Date(app.dateDerniereLivraison).getTime() : 0;
      default:
        return '';
    }
  }
}
