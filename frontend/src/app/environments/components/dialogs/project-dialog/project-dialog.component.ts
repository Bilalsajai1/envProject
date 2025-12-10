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
  envTypeOptions: { code: string; libelle: string; disabled: boolean }[] = [];

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
      envTypeCodes: [[], Validators.required]
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
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const envTypeCodes: string[] = this.form.value.envTypeCodes ?? [];
    const primaryType = envTypeCodes.length > 0 ? envTypeCodes[0] : this.data.typeCode;
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

  // =========================================================
  // Types d'environnement
  // =========================================================
  isTypeSelected(code: string): boolean {
    const selected: string[] = this.form.value.envTypeCodes ?? [];
    return selected.includes(code);
  }

  toggleEnvType(code: string, checked: boolean): void {
    const current: string[] = [...(this.form.value.envTypeCodes ?? [])];
    const next = checked ? Array.from(new Set([...current, code])) : current.filter(c => c !== code);
    this.form.patchValue({ envTypeCodes: next });
  }

  private buildEnvTypeOptions(): void {
    const ctx = this.authContext.getCurrentContext();
    const isAdmin = ctx?.user?.admin;
    this.envTypeOptions = (ctx?.environmentTypes ?? []).map(t => ({
      code: t.code,
      libelle: t.libelle,
      disabled: !isAdmin && !t.allowedActions.includes('CREATE')
    }));
  }
}
