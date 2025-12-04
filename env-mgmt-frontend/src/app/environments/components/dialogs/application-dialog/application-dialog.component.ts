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

  protocols = ['HTTP', 'HTTPS', 'FTP', 'SFTP', 'SSH'];
  showApplicationCustom = false;
  showProtocolCustom = false;

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
      applicationCustom: [''],
      protocole: ['', Validators.required],
      protocoleCustom: [''],
      host: [''],
      port: [null],
      username: [''],
      password: [''],
      url: [''],
      dateDerniereLivraison: [''],
      description: [''],
      actif: [true]
    });
  }

  ngOnInit(): void {
    this.loadAvailableApplications();
    this.setupFormListeners();

    if (this.data.application) {
      this.isEdit = true;
      this.initializeEditMode();
    }
  }

  private setupFormListeners(): void {
    this.form.get('applicationId')?.valueChanges.subscribe(value => {
      this.showApplicationCustom = value === 'AUTRE';
      if (!this.showApplicationCustom) {
        this.form.get('applicationCustom')?.reset();
      }
    });

    this.form.get('protocole')?.valueChanges.subscribe(value => {
      this.showProtocolCustom = value === 'AUTRE';
      if (!this.showProtocolCustom) {
        this.form.get('protocoleCustom')?.reset();
      }
    });
  }

  private initializeEditMode(): void {
    if (this.data.application) {
      this.form.patchValue({
        applicationId: this.data.application.applicationId,
        protocole: this.data.application.protocole,
        host: this.data.application.host,
        port: this.data.application.port,
        username: this.data.application.username,
        password: this.data.application.password,
        url: this.data.application.url,
        dateDerniereLivraison: this.data.application.dateDerniereLivraison,
        description: this.data.application.description,
        actif: this.data.application.actif
      });

      this.form.get('applicationId')?.disable();
    }
  }

  loadAvailableApplications(): void {
    this.applicationService.getAllApplications().subscribe({
      next: (apps) => {
        this.availableApplications = apps;
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement des applications', 'Fermer', { duration: 3000 });
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;

    const formValue = this.form.getRawValue();

    const payload: Partial<EnvApplicationDTO> = {
      applicationId: this.showApplicationCustom ? null : formValue.applicationId,
      applicationLibelle: this.showApplicationCustom ? formValue.applicationCustom : undefined,
      protocole: this.showProtocolCustom ? formValue.protocoleCustom : formValue.protocole,
      host: formValue.host,
      port: formValue.port,
      username: formValue.username,
      password: formValue.password,
      url: formValue.url,
      dateDerniereLivraison: formValue.dateDerniereLivraison,
      description: formValue.description,
      actif: formValue.actif,
      environnementId: this.data.environmentId
    };

    const obs = this.isEdit && this.data.application
      ? this.applicationService.update(this.data.application.id, payload)
      : this.applicationService.create(payload);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          `Application ${this.isEdit ? 'modifiée' : 'ajoutée'} avec succès`,
          'Fermer',
          { duration: 3000 }
        );
        this.dialogRef.close(true);
      },
      error: (err) => {
        const message = err.error?.message || 'Erreur lors de la sauvegarde';
        this.snackBar.open(message, 'Fermer', { duration: 3000 });
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
