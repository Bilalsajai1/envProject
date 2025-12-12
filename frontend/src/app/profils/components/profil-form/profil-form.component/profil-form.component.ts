
import { Component, OnInit, Inject, Optional } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ProfilCreateUpdateDTO, ProfilDTO, ProfilService } from '../../../services/profil.service';

interface ProfilFormData {
  mode: 'create' | 'edit';
  profilId?: number;
}

@Component({
  selector: 'app-profil-form',
  standalone: false,
  templateUrl: './profil-form.component.html',
  styleUrls: ['./profil-form.component.scss']
})
export class ProfilFormComponent implements OnInit {

  form!: FormGroup;
  isEdit = false;
  profilId?: number;

  loading = false;
  saving = false;
  private isDialog = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private profilService: ProfilService,
    private snackBar: MatSnackBar,
    @Optional() private dialogRef?: MatDialogRef<ProfilFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data?: ProfilFormData
  ) {}

  ngOnInit(): void {
    this.buildForm();

    if (this.data) {
      this.isDialog = true;
      this.isEdit = this.data.mode === 'edit';
      this.profilId = this.data.profilId;
    } else {
      const idParam = this.route.snapshot.paramMap.get('id');
      if (idParam) {
        this.isEdit = true;
        this.profilId = Number(idParam);
      }
    }

    if (this.isEdit && this.profilId) {
      this.loadProfil(this.profilId);
    }
  }

  buildForm(): void {
    this.form = this.fb.group({
      code: ['', Validators.required],
      libelle: ['', Validators.required],
      description: [''],
      admin: [false],
      actif: [true]
    });
  }

  loadProfil(id: number): void {
    this.loading = true;
    this.profilService.getById(id).subscribe({
      next: (profil: ProfilDTO) => {
        this.form.patchValue({
          code: profil.code,
          libelle: profil.libelle,
          description: profil.description,
          admin: profil.admin,
          actif: profil.actif
        });
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const payload: ProfilCreateUpdateDTO = this.form.value;

    const obs = this.isEdit && this.profilId
      ? this.profilService.update(this.profilId, payload)
      : this.profilService.create(payload);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          `Profil ${this.isEdit ? 'modifié' : 'créé'} avec succès`,
          'Fermer',
          { duration: 3000 }
        );
        this.saving = false;
        if (this.isDialog) {
          this.dialogRef?.close(true);
        } else {
          this.router.navigate(['/admin/profils']);
        }
      },
      error: () => {
        this.snackBar.open('Erreur lors de la sauvegarde', 'Fermer', { duration: 3000 });
        this.saving = false;
      }
    });
  }

  cancel(): void {
    if (this.isDialog) {
      this.dialogRef?.close(false);
    } else {
      this.router.navigate(['/admin/profils']);
    }
  }
}
