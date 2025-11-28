// src/app/environments/components/application-list/application-list.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {ApplicationService} from '../services/application.service';
import {EnvironmentService} from '../services/environment.service';
import {EnvApplicationDTO, EnvironmentDTO} from '../models/environment.model';
import {ConfirmDialogComponent} from '../../users/confirm-dialog/confirm-dialog.component';
import {ApplicationDialogComponent} from '../components/dialogs/application-dialog/application-dialog.component';


@Component({
  selector: 'app-application-list',
  standalone: false,
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.scss']
})
export class ApplicationListComponent implements OnInit {

  typeCode: string = '';
  projectId!: number;
  environmentId!: number;
  environment?: EnvironmentDTO;
  applications: EnvApplicationDTO[] = [];
  loading = false;

  displayedColumns = [
    'applicationCode',
    'applicationLibelle',
    'protocole',
    'host',
    'port',
    'actif',
    'actions'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private applicationService: ApplicationService,
    private environmentService: EnvironmentService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.projectId = Number(params.get('projectId'));
      this.environmentId = Number(params.get('environmentId'));
      this.loadApplications();
    });

    this.route.parent?.parent?.parent?.paramMap.subscribe(params => {
      this.typeCode = params.get('typeCode')?.toUpperCase() || '';
    });
  }

  loadApplications(): void {
    this.loading = true;
    this.applicationService.getByEnvironment(this.environmentId).subscribe({
      next: (applications) => {
        this.applications = applications;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('❌ Erreur lors du chargement des applications', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  addApplication(): void {
    const dialogRef = this.dialog.open(ApplicationDialogComponent, {
      width: '700px',
      data: {
        environmentId: this.environmentId
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadApplications();
      }
    });
  }

  editApplication(app: EnvApplicationDTO): void {
    const dialogRef = this.dialog.open(ApplicationDialogComponent, {
      width: '700px',
      data: {
        application: app,
        environmentId: this.environmentId
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadApplications();
      }
    });
  }

  deleteApplication(app: EnvApplicationDTO): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirmer la suppression',
        message: `Voulez-vous vraiment supprimer l'application "${app.applicationLibelle}" ?`,
        confirmText: 'Supprimer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.applicationService.delete(app.id).subscribe({
          next: () => {
            this.snackBar.open('✅ Application supprimée avec succès', 'Fermer', { duration: 3000 });
            this.loadApplications();
          },
          error: () => {
            this.snackBar.open('❌ Erreur lors de la suppression', 'Fermer', { duration: 3000 });
          }
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['../../../'], { relativeTo: this.route });
  }
}
