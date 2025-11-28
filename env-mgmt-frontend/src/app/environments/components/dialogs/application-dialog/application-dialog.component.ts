// src/app/environments/components/dialogs/application-dialog/application-dialog.component.ts

import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApplicationService } from '../../../services/application.service';
import { ApplicationDTO, EnvApplicationDTO } from '../../../models/environment.model';

@Component({
  selector: 'app-application-dialog',
  standalone: false,
  templateUrl: './application-dialog.component.html',
  styleUrls: ['./application-dialog.component.scss']
})
export class ApplicationDialogComponent implements OnInit {

  form: FormGroup;
  isEdit = false;
  saving = false;
  availableApplications: ApplicationDTO[] = [];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ApplicationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      application?: EnvApplicationDTO;
      environmentId: number
    },
    private applicationService: ApplicationService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      applicationId: [null, Validators.required],
      protocole: [''],
      host: [''],
      port: [null],
      url: [''],
      username: [''],
      password: [''],
      description: [''],
      actif: [true]
    });
  }

  ngOnInit(): void {
    this.loadAvailableApplications();

    if (this.data.application) {
      this.isEdit = true;
      this.form.patchValue(this.data.application);
      // En mode édition, désactiver le choix d'application
      this.form.get('applicationId')?.disable();
    }
  }

  loadAvailableApplications(): void {
    this.applicationService.getAllApplications().subscribe({
      next: (apps) => {
        this.availableApplications = apps;
      },
      error: () => {
        this.snackBar.open('❌ Erreur lors du chargement des applications', 'Fermer', { duration: 3000 });
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;

    const payload: Partial<EnvApplicationDTO> = {
      ...this.form.getRawValue(), // getRawValue() pour récupérer même les champs disabled
      environnementId: this.data.environmentId
    };

    const obs = this.isEdit && this.data.application
      ? this.applicationService.update(this.data.application.id, payload)
      : this.applicationService.create(payload);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          `✅ Application ${this.isEdit ? 'modifiée' : 'ajoutée'} avec succès`,
          'Fermer',
          { duration: 3000 }
        );
        this.dialogRef.close(true);
      },
      error: (err) => {
        const message = err.error?.message || 'Erreur lors de la sauvegarde';
        this.snackBar.open(`❌ ${message}`, 'Fermer', { duration: 3000 });
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
