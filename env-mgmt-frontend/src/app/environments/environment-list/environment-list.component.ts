// src/app/environments/components/environment-list/environment-list.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {EnvironmentDTO, ProjectDTO} from '../models/environment.model';
import {EnvironmentService} from '../services/environment.service';
import {ProjectService} from '../services/project.service';
import {EnvironmentDialogComponent} from '../components/dialogs/environment-dialog/environment-dialog.component';
import {ConfirmDialogComponent} from '../../users/confirm-dialog/confirm-dialog.component';


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
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.projectId = Number(params.get('projectId'));
      this.loadProject();
    });

    this.route.parent?.parent?.paramMap.subscribe(params => {
      this.typeCode = params.get('typeCode')?.toUpperCase() || '';
      if (this.projectId) {
        this.loadEnvironments();
      }
    });
  }

  loadProject(): void {
    this.projectService.getById(this.projectId).subscribe({
      next: (project) => {
        this.project = project;
      },
      error: () => {
        this.snackBar.open('❌ Projet introuvable', 'Fermer', { duration: 3000 });
      }
    });
  }

  loadEnvironments(): void {
    this.loading = true;
    this.environmentService.getByProjectAndType(this.projectId, this.typeCode).subscribe({
      next: (environments) => {
        this.environments = environments;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('❌ Erreur lors du chargement des environnements', 'Fermer', { duration: 3000 });
        this.loading = false;
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
