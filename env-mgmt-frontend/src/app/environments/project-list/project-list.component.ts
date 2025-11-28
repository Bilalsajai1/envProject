// src/app/environments/components/project-list/project-list.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {ProjectDTO} from '../models/environment.model';
import {ProjectService} from '../services/project.service';
import {ProjectDialogComponent} from '../components/dialogs/project-dialog/project-dialog.component';
import {ConfirmDialogComponent} from '../../users/confirm-dialog/confirm-dialog.component';


@Component({
  selector: 'app-project-list',
  standalone: false,
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss']
})
export class ProjectListComponent implements OnInit {

  typeCode: string = '';
  projects: ProjectDTO[] = [];
  loading = false;

  displayedColumns = ['code', 'libelle', 'description', 'actif', 'actions'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.parent?.paramMap.subscribe(params => {
      this.typeCode = params.get('typeCode')?.toUpperCase() || '';
      this.loadProjects();
    });
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.getByEnvironmentType(this.typeCode).subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('❌ Erreur lors du chargement des projets', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
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

  editProject(event: Event, project: ProjectDTO): void {
    event.stopPropagation();

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

  deleteProject(event: Event, project: ProjectDTO): void {
    event.stopPropagation();

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
            this.snackBar.open('✅ Projet supprimé avec succès', 'Fermer', { duration: 3000 });
            this.loadProjects();
          },
          error: () => {
            this.snackBar.open('❌ Erreur lors de la suppression', 'Fermer', { duration: 3000 });
          }
        });
      }
    });
  }
}
