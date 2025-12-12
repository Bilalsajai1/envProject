
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  Subject,
  takeUntil,
  finalize,
  distinctUntilChanged
} from 'rxjs';
import { Router } from '@angular/router';

import { ProfilDTO, ProfilService } from '../../../services/profil.service';
import { PaginatedResponse } from '../../../../users/models/user.model';
import { ConfirmDialogComponent } from '../../../../users/confirm-dialog/confirm-dialog.component';
import { ProfilFormComponent } from '../../profil-form/profil-form.component/profil-form.component';

type SortDirection = 'asc' | 'desc';

interface ProfilSearchRequest {
  page: number;
  size: number;
  sortField: string;
  sortDirection: SortDirection;
  filters: Record<string, any>;
}

@Component({
  selector: 'app-profil-list',
  standalone: false,
  templateUrl: './profil-list.component.html',
  styleUrls: ['./profil-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfilListComponent implements OnInit, OnDestroy {

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  profils: ProfilDTO[] = [];
  displayedColumns: string[] = [
    'code',
    'libelle',
    'description',
    'admin',
    'nbUsers',
    'actif',
    'actions'
  ];

  page = 0;
  size = 10;
  totalElements = 0;
  sortField = 'id';
  sortDirection: SortDirection = 'asc';

  searchTerm = '';
  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  loading = false;

  constructor(
    private readonly profilService: ProfilService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.initSearchListener();
    this.loadProfils();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initSearchListener(): void {
    this.searchSubject
      .pipe(
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(term => {
        this.searchTerm = (term || '').trim();
        this.page = 0;
        this.loadProfils();
      });
  }

  private buildFilters(): Record<string, any> {
    const filters: Record<string, any> = {};
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      filters['search'] = this.searchTerm.trim();
    }
    filters['onlyActiveUsers'] = true;
    return filters;
  }

  loadProfils(): void {
    const request: ProfilSearchRequest = {
      page: this.page,
      size: this.size,
      sortField: this.sortField,
      sortDirection: this.sortDirection,
      filters: this.buildFilters()
    };

    this.loading = true;

    this.profilService
      .search(request)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (res: PaginatedResponse<ProfilDTO>) => {
          this.profils = res.content ?? [];
          this.totalElements = res.totalElements ?? 0;
          this.page = res.page ?? 0;
          this.size = res.size ?? this.size;
          this.cdr.markForCheck();
        },
        error: () => {
          this.showSnackBar('Erreur lors du chargement des profils', 'error-snackbar');
        }
      });
  }

  onSearchChange(value: string): void {
    this.searchSubject.next(value ?? '');
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadProfils();
  }

  onSortChange(sort: Sort): void {
    if (sort.active && sort.direction) {
      this.sortField = sort.active;
      this.sortDirection = sort.direction as SortDirection;
    } else {
      this.sortField = 'id';
      this.sortDirection = 'asc';
    }
    this.loadProfils();
  }

  addProfil(): void {
    this.openProfilForm('create');
  }

  editProfil(profil: ProfilDTO): void {
    if (!profil.id) return;
    this.openProfilForm('edit', profil.id);
  }

  private openProfilForm(mode: 'create' | 'edit', profilId?: number): void {
    const dialogRef = this.dialog.open(ProfilFormComponent, {
      width: '900px',
      maxWidth: '80vw',
      disableClose: true,
      panelClass: 'profil-form-dialog',
      data: {
        mode,
        profilId
      }
    });

    this.cdr.markForCheck();

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe(shouldReload => {
        if (shouldReload) {
          this.loadProfils();
        } else {
          this.cdr.markForCheck();
        }
      });
  }

  configurePermissions(profil: ProfilDTO): void {
    if (!profil.id) return;
    this.router.navigate(
      ['/admin/permissions'],
      { queryParams: { profilId: profil.id } }
    );
  }

  deleteProfil(profil: ProfilDTO): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Voulez-vous vraiment supprimer le profil "${profil.libelle}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe(confirmed => {
        if (!confirmed || !profil.id) return;

        this.profilService.delete(profil.id).subscribe({
          next: () => {
            this.showSnackBar('Profil supprime avec succes', 'success-snackbar');
            this.loadProfils();
          },
          error: (err) => {
            const message = err?.error?.message || 'Impossible de supprimer ce profil (des utilisateurs actifs y sont peut-être associés).';
            this.showSnackBar(message, 'error-snackbar');
          }
        });
      });
  }

  get isEmpty(): boolean {
    return !this.loading && this.profils.length === 0;
  }

  get resultsLabel(): string {
    const count = this.totalElements || 0;
    if (count === 0) return 'Aucun profil';
    if (count === 1) return '1 profil';
    return `${count} profils`;
  }

  trackByProfil(_: number, profil: ProfilDTO): number | undefined {
    return profil.id;
  }

  private showSnackBar(message: string, panelClass: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: [panelClass]
    });
  }
}
