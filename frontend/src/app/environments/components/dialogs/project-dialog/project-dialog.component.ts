// src/app/environments/components/dialogs/project-dialog/project-dialog.component.ts

import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectService } from '../../../services/project.service';
import { ProjectDTO } from '../../../models/environment.model';
import { AuthContextService } from '../../../../auth/services/auth-context.service';

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
  envTypeOptions: { code: string; libelle: string }[] = [];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ProjectDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { project?: ProjectDTO; typeCode: string },
    private projectService: ProjectService,
    private snackBar: MatSnackBar,
    private authContext: AuthContextService
  ) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      libelle: ['', Validators.required],
      description: [''],
      actif: [true],
      envTypeCodes: [{ value: [], disabled: true }, Validators.required]
    });
  }

  ngOnInit(): void {
    this.buildEnvTypeOptions();

    const initialTypes = this.data.project?.envTypeCodes?.length
      ? this.data.project.envTypeCodes
      : this.data.typeCode
        ? [this.data.typeCode]
        : [];

    this.form.patchValue({ envTypeCodes: initialTypes });

    if (this.data.project) {
      this.isEdit = true;
      this.form.patchValue(this.data.project);
    } else if (this.data.typeCode) {
      this.form.patchValue({ envTypeCodes: [this.data.typeCode] });
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const envTypeCodes: string[] = [this.data.typeCode].filter(Boolean) as string[];
    const primaryType = this.data.typeCode;
    const payload = {
      ...this.form.value,
      envTypeCode: primaryType,
      envTypeCodes
    };

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

  private buildEnvTypeOptions(): void {
    const ctx = this.authContext.getCurrentContext();
    const current = ctx?.environmentTypes?.find(t => t.code?.toUpperCase() === this.data.typeCode?.toUpperCase());
    if (current) {
      this.envTypeOptions = [{ code: current.code, libelle: current.libelle }];
    } else if (this.data.typeCode) {
      this.envTypeOptions = [{ code: this.data.typeCode, libelle: this.data.typeCode }];
    } else {
      this.envTypeOptions = [];
    }
  }
}
