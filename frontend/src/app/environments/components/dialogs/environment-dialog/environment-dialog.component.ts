
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EnvironmentService } from '../../../services/environment.service';
import { EnvironmentDTO } from '../../../models/environment.model';

@Component({
  selector: 'app-environment-dialog',
  standalone: false,
  templateUrl: './environment-dialog.component.html',
  styleUrls: ['./environment-dialog.component.scss']
})
export class EnvironmentDialogComponent implements OnInit {

  form: FormGroup;
  isEdit = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<EnvironmentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      environment?: EnvironmentDTO;
      projectId: number;
      typeCode: string
    },
    private environmentService: EnvironmentService,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      libelle: ['', Validators.required],
      description: [''],
      actif: [true]
    });
  }

  ngOnInit(): void {
    if (this.data.environment) {
      this.isEdit = true;
      this.form.patchValue(this.data.environment);
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const payload = {
      ...this.form.value,
      projetId: this.data.projectId,
      typeCode: this.data.typeCode
    };

    const obs = this.isEdit && this.data.environment
      ? this.environmentService.update(this.data.environment.id, payload)
      : this.environmentService.create(payload);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          `✅ Environnement ${this.isEdit ? 'modifié' : 'créé'} avec succès`,
          'Fermer',
          { duration: 3000 }
        );
        this.dialogRef.close(true);
      },
      error: () => {
        this.snackBar.open('❌ Erreur lors de la sauvegarde', 'Fermer', { duration: 3000 });
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
