// src/app/environments/components/environment-list/environment-list.component.ts

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EnvironmentDTO, ProjectDTO } from '../models/environment.model';
import { EnvironmentService } from '../services/environment.service';
import { ProjectService } from '../services/project.service';
import { EnvironmentDialogComponent } from '../components/dialogs/environment-dialog/environment-dialog.component';
import { ConfirmDialogComponent } from '../../users/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-environment-list',
  standalone: false,
  templateUrl: './environment-list.component.html',
  styleUrls: ['./environment-list.component.scss']
})
export class EnvironmentListComponent implements OnInit {

  typeCode: string = '';
  projectId!: number;
  project?: ProjectDTO;
  environments: EnvironmentDTO[] = [];
  loading = false;

  displayedColumns = ['code', 'libelle', 'description', 'actif', 'actions'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private environmentService: EnvironmentService,
    private projectService: ProjectService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef  // ✅ Ajout pour gérer NG0100
  ) {}

  ngOnInit(): void {
    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));

    // Récupérer typeCode depuis le parent
    let parentRoute = this.route.parent;
    console.log('🔍 Debug routes:', {
      currentRoute: this.route.snapshot.url,
      parentRoute: parentRoute?.snapshot.url,
      hasParent: !!parentRoute
    });

    // Remonter jusqu'à trouver le typeCode
    while (parentRoute) {
      const typeCodeParam = parentRoute.snapshot.paramMap.get('typeCode');
      console.log('🔍 Checking parent for typeCode:', {
        url: parentRoute.snapshot.url,
        typeCode: typeCodeParam
      });

      if (typeCodeParam) {
        this.typeCode = typeCodeParam.toUpperCase();
        break;
      }
      parentRoute = parentRoute.parent;
    }

    console.log('✅ Final params:', {
      projectId: this.projectId,
      typeCode: this.typeCode
    });

    if (this.projectId && this.typeCode) {
      // ✅ Charger les environnements en premier (permission garantie)
      this.loadEnvironments();

      // ✅ Essayer de charger le projet (peut échouer en 403, ce n'est pas grave)
      this.loadProject();
    } else {
      console.error('❌ projectId ou typeCode manquant !', {
        projectId: this.projectId,
        typeCode: this.typeCode
      });
      this.snackBar.open('❌ Paramètres de route manquants', 'Fermer', { duration: 3000 });
    }
  }

  loadProject(): void {
    this.projectService.getById(this.projectId).subscribe({
      next: (project) => {
        this.project = project;
        // ✅ Forcer la détection de changement
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.warn('⚠️ Impossible de charger les détails du projet (403 attendu pour non-admin):', err);
        // ✅ Ne pas afficher d'erreur si 403 (permissions insuffisantes)
        if (err.status !== 403) {
          this.snackBar.open('❌ Projet introuvable', 'Fermer', { duration: 3000 });
        }
        // ✅ Créer un projet "minimal" avec juste l'ID pour l'affichage
        this.project = {
          id: this.projectId,
          code: `PROJET_${this.projectId}`,
          libelle: `Projet ${this.projectId}`,
          actif: true
        };
        this.cdr.detectChanges();
      }
    });
  }

  loadEnvironments(): void {
    // ✅ Utiliser setTimeout pour éviter NG0100
    setTimeout(() => {
      this.loading = true;
      this.cdr.detectChanges();
    });

    console.log('📡 Appel API getByProjectAndType:', {
      projectId: this.projectId,
      typeCode: this.typeCode
    });

    this.environmentService.getByProjectAndType(this.projectId, this.typeCode).subscribe({
      next: (environments) => {
        console.log('✅ Environnements récupérés:', environments);
        this.environments = environments;

        // ✅ Utiliser setTimeout pour éviter NG0100
        setTimeout(() => {
          this.loading = false;
          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        console.error('❌ Erreur lors du chargement des environnements:', err);
        this.snackBar.open('❌ Erreur lors du chargement des environnements', 'Fermer', { duration: 3000 });

        // ✅ Utiliser setTimeout pour éviter NG0100
        setTimeout(() => {
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  openEnvironment(env: EnvironmentDTO): void {
    this.router.navigate([env.id, 'applications'], {
      relativeTo: this.route
    });
  }

  addEnvironment(): void {
    const dialogRef = this.dialog.open(EnvironmentDialogComponent, {
      width: '600px',
      data: {
        projectId: this.projectId,
        typeCode: this.typeCode
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadEnvironments();
      }
    });
  }

  editEnvironment(event: Event, env: EnvironmentDTO): void {
    event.stopPropagation();

    const dialogRef = this.dialog.open(EnvironmentDialogComponent, {
      width: '600px',
      data: {
        environment: env,
        projectId: this.projectId,
        typeCode: this.typeCode
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadEnvironments();
      }
    });
  }

  deleteEnvironment(event: Event, env: EnvironmentDTO): void {
    event.stopPropagation();

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Voulez-vous vraiment supprimer l'environnement "${env.libelle}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.environmentService.delete(env.id).subscribe({
          next: () => {
            this.snackBar.open('✅ Environnement supprimé avec succès', 'Fermer', { duration: 3000 });
            this.loadEnvironments();
          },
          error: () => {
            this.snackBar.open('❌ Erreur lors de la suppression', 'Fermer', { duration: 3000 });
          }
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['../../'], { relativeTo: this.route });
  }
}
