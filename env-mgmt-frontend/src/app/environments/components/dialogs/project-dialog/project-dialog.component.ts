// src/app/environments/components/dialogs/project-dialog/project-dialog.component.ts

import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectService } from '../../../services/project.service';
import { ProjectDTO } from '../../../models/environment.model';

@Component({
  selector: 'app-project-dialog',
  standalone: false,
  templateUrl: './project-dialog.component.html',
  styleUrls: ['./project-dialog.component.scss']
})
export class ProjectDialogComponent implements OnInit {

  form: FormGroup;
  isEdit = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ProjectDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { project?: ProjectDTO; typeCode: string },
    private projectService: ProjectService,
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
    if (this.data.project) {
      this.isEdit = true;
      this.form.patchValue(this.data.project);
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const payload = this.form.value;

    const obs = this.isEdit && this.data.project
      ? this.projectService.update(this.data.project.id, payload)
      : this.projectService.create(payload);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          `✅ Projet ${this.isEdit ? 'modifié' : 'créé'} avec succès`,
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
