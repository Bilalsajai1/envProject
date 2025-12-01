// src/app/profils/components/profil-form/profil-form.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import {ProfilCreateUpdateDTO, ProfilDTO, ProfilService} from '../../../services/profil.service';

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

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private profilService: ProfilService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.buildForm();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = true;
      this.profilId = Number(idParam);
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
        this.snackBar.open('❌ Erreur lors du chargement', 'Fermer', { duration: 3000 });
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
          `✅ Profil ${this.isEdit ? 'modifié' : 'créé'} avec succès`,
          'Fermer',
          { duration: 3000 }
        );
        this.saving = false;
        this.router.navigate(['/admin/profils']);
      },
      error: () => {
        this.snackBar.open('❌ Erreur lors de la sauvegarde', 'Fermer', { duration: 3000 });
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/profils']);
  }
}
