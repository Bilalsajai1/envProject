import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ProfilService } from '../../environement/services/profil-service';
import { Profil } from '../../environement/models/profil.model';

@Component({
  selector: 'app-profile-admin',
  standalone: false,
  templateUrl: './profile-admin.component.html',
  styleUrls: ['./profile-admin.component.scss'],
})
export class ProfileAdminComponent implements OnInit {

  profils: Profil[] = [];
  loading = false;
  showForm = false;
  editMode = false;
  currentId?: number;

  form: Profil = {
    id: 0,
    code: '',
    libelle: '',
    description: '',
    admin: false,
    actif: true
  };

  constructor(
    private profilService: ProfilService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadProfils();
  }

  loadProfils(): void {
    this.loading = true;
    this.profilService.getAll().subscribe({
      next: (list: Profil[]) => {
        this.profils = list;
        this.loading = false;
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des profils.');
        this.loading = false;
      }
    });
  }

  openCreate(): void {
    this.showForm = true;
    this.editMode = false;
    this.currentId = undefined;

    this.form = {
      id: 0,
      code: '',
      libelle: '',
      description: '',
      admin: false,
      actif: true
    };
  }

  openEdit(p: Profil): void {
    this.showForm = true;
    this.editMode = true;
    this.currentId = p.id;
    this.form = { ...p };
  }

  cancel(): void {
    this.showForm = false;
    this.editMode = false;
    this.currentId = undefined;
  }

  save(): void {
    if (!this.form.code || !this.form.libelle) {
      this.toastr.warning('Code et libellé sont obligatoires');
      return;
    }

    const payload: Partial<Profil> = {
      code: this.form.code,
      libelle: this.form.libelle,
      description: this.form.description,
      admin: this.form.admin,
      actif: this.form.actif
    };

    if (this.editMode && this.currentId) {
      this.profilService.update(this.currentId, payload).subscribe({
        next: () => {
          this.toastr.success('Profil mis à jour');
          this.showForm = false;
          this.loadProfils();
        },
        error: (err: any) => {
          console.error(err);
          const msg = err.error?.message || 'Erreur lors de la mise à jour du profil';
          this.toastr.error(msg);
        }
      });
    } else {
      this.profilService.create(payload).subscribe({
        next: () => {
          this.toastr.success('Profil créé');
          this.showForm = false;
          this.loadProfils();
        },
        error: (err: any) => {
          console.error(err);
          const msg = err.error?.message || 'Erreur lors de la création du profil';
          this.toastr.error(msg);
        }
      });
    }
  }

  delete(p: Profil): void {
    if (!p.id) {
      return;
    }
    if (!confirm(`Supprimer le profil ${p.code} ?`)) {
      return;
    }

    this.profilService.delete(p.id).subscribe({
      next: () => {
        this.toastr.success('Profil supprimé');
        this.loadProfils();
      },
      error: (err: any) => {
        console.error(err);
        const msg = err.error?.message || 'Erreur lors de la suppression du profil';
        this.toastr.error(msg);
      }
    });
  }
}
