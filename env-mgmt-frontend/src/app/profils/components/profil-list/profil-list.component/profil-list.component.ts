// src/app/profils/components/profil-list/profil-list.component.ts

import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import {ProfilDTO, ProfilService} from '../../../services/profil.service';
import {PaginatedResponse} from '../../../../users/models/user.model';
import {ConfirmDialogComponent} from '../../../../users/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-profil-list',
  standalone: false,
  templateUrl: './profil-list.component.html',
  styleUrls: ['./profil-list.component.scss']
})
export class ProfilListComponent implements OnInit {

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
  sortDirection: 'asc' | 'desc' = 'asc';

  searchTerm = '';
  private searchSubject = new Subject<string>();

  loading = false;

  constructor(
    private profilService: ProfilService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(term => {
      this.searchTerm = term;
      this.page = 0;
      this.loadProfils();
    });
  }

  ngOnInit(): void {
    this.loadProfils();
  }

  loadProfils(): void {
    this.loading = true;

    const filters: any = {};
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      filters.search = this.searchTerm.trim();
    }

    this.profilService.search({
      page: this.page,
      size: this.size,
      sortField: this.sortField,
      sortDirection: this.sortDirection,
      filters
    }).subscribe({
      next: (res: PaginatedResponse<ProfilDTO>) => {
        this.profils = res.content;
        this.totalElements = res.totalElements;
        this.page = res.page;
        this.size = res.size;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('❌ Erreur lors du chargement', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadProfils();
  }

  onSortChange(sort: Sort): void {
    if (sort.active && sort.direction) {
      this.sortField = sort.active;
      this.sortDirection = sort.direction as 'asc' | 'desc';
    } else {
      this.sortField = 'id';
      this.sortDirection = 'asc';
    }
    this.loadProfils();
  }

  addProfil(): void {
    this.router.navigate(['/admin/profils/new']);
  }

  editProfil(profil: ProfilDTO): void {
    this.router.navigate(['/admin/profils', profil.id, 'edit']);
  }

  configurePermissions(profil: ProfilDTO): void {
    this.router.navigate(['/admin/permissions'], {
      queryParams: { profilId: profil.id }
    });
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

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.profilService.delete(profil.id).subscribe({
          next: () => {
            this.snackBar.open('✅ Profil supprimé avec succès', 'Fermer', { duration: 3000 });
            this.loadProfils();
          },
          error: () => {
            this.snackBar.open('❌ Erreur lors de la suppression', 'Fermer', { duration: 3000 });
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
