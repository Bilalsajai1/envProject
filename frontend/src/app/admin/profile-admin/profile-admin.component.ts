import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Profil } from '../../environement/models/profil.model';
import { ProfilService } from '../../environement/services/profil-service';
import { Role } from '../../environement/models/role.model';
import { RoleService } from '../../environement/services/role-service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-profile-admin',
  standalone: false,
  templateUrl: './profile-admin.component.html',
  styleUrl: './profile-admin.component.scss',
})
export class ProfileAdminComponent implements OnInit {

  // Liste des profils
  profils: Profil[] = [];
  loading = false;
  error?: string;

  // Formulaire CRUD
  showForm = false;
  isEditing = false;
  currentId?: number;

  form: Partial<Profil> = {
    code: '',
    libelle: '',
    description: '',
    admin: false,
    actif: true
  };

  // Panneau rôles
  showRolePanel = false;
  selectedProfil?: Profil;

  roles: Role[] = [];
  selectedRoleIds: number[] = [];
  loadingRoles = false;

  constructor(
    private profilService: ProfilService,
    private roleService: RoleService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = undefined;

    this.profilService.getAll().subscribe({
      next: (data) => {
        this.profils = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Erreur lors du chargement des profils.';
        this.toastr.error(this.error);
        this.loading = false;
      }
    });
  }

  // ---------- CRUD PROFIL ----------

  openCreateForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.currentId = undefined;
    this.form = {
      code: '',
      libelle: '',
      description: '',
      admin: false,
      actif: true
    };
  }

  openEditForm(p: Profil): void {
    this.showForm = true;
    this.isEditing = true;
    this.currentId = p.id;
    this.form = { ...p };
  }

  cancelForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.currentId = undefined;
  }

  submitForm(): void {
    if (!this.form.code || !this.form.libelle) {
      this.toastr.warning('Code et libellé sont obligatoires.');
      return;
    }

    const payload: Partial<Profil> = {
      code: this.form.code,
      libelle: this.form.libelle,
      description: this.form.description,
      admin: this.form.admin ?? false,
      actif: this.form.actif ?? true
    };

    if (this.isEditing && this.currentId) {
      this.profilService.update(this.currentId, payload).subscribe({
        next: () => {
          this.toastr.success('Profil mis à jour avec succès.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la mise à jour du profil.';
          this.toastr.error(msg);
        }
      });
    } else {
      this.profilService.create(payload).subscribe({
        next: () => {
          this.toastr.success('Profil créé avec succès.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la création du profil.';
          this.toastr.error(msg);
        }
      });
    }
  }

  deleteProfil(p: Profil): void {
    if (!confirm(`Supprimer le profil ${p.code} ?`)) {
      return;
    }

    this.profilService.delete(p.id).subscribe({
      next: () => {
        this.toastr.success('Profil supprimé avec succès.');
        this.load();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression du profil.';
        this.toastr.error(msg);
      }
    });
  }

  // ---------- PANNEAU RÔLES ----------

  openRolePanel(p: Profil): void {
    this.selectedProfil = p;
    this.showRolePanel = true;
    this.loadingRoles = true;

    forkJoin({
      roles: this.roleService.getAllRoles(),
      roleIds: this.profilService.getRoleIdsForProfil(p.id)
    }).subscribe({
      next: ({ roles, roleIds }) => {
        this.roles = roles;
        this.selectedRoleIds = roleIds ?? [];
        this.loadingRoles = false;
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des rôles du profil.');
        this.loadingRoles = false;
      }
    });
  }

  closeRolePanel(): void {
    this.showRolePanel = false;
    this.selectedProfil = undefined;
    this.roles = [];
    this.selectedRoleIds = [];
  }

  onToggleRole(roleId: number, checked: boolean): void {
    if (checked) {
      if (!this.selectedRoleIds.includes(roleId)) {
        this.selectedRoleIds.push(roleId);
      }
    } else {
      this.selectedRoleIds = this.selectedRoleIds.filter(id => id !== roleId);
    }
  }

  saveRoles(): void {
    if (!this.selectedProfil) {
      return;
    }

    this.profilService.updateRoles(this.selectedProfil.id, this.selectedRoleIds).subscribe({
      next: () => {
        this.toastr.success('Rôles du profil mis à jour.');
        this.closeRolePanel();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la mise à jour des rôles.';
        this.toastr.error(msg);
      }
    });
  }
}
