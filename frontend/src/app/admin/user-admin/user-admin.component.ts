import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { User } from '../../environement/models/user.model';
import { UserService } from '../../environement/services/user-service';
import { Profil } from '../../environement/models/profil.model';
import { ProfilService } from '../../environement/services/profil-service';

@Component({
  selector: 'app-user-admin',
  standalone: false,
  templateUrl: './user-admin.component.html',
  styleUrls: ['./user-admin.component.scss']
})
export class UserAdminComponent implements OnInit {

  // Liste users (colonne gauche)
  users: User[] = [];
  loadingUsers = false;
  userError?: string;
  search = '';

  // Sélection
  selectedUser?: User;

  // Formulaire CRUD user
  showForm = false;
  isEditing = false;
  currentId?: number;

  form: Partial<User> = {
    code: '',
    firstName: '',
    lastName: '',
    email: '',
    actif: true,
    profilId: undefined
  };

  // Profils pour choisir dans un select
  profils: Profil[] = [];
  loadingProfils = false;

  // Tabs
  activeTab: 'INFO' | 'PERMISSIONS' | 'ROLES' = 'INFO';

  constructor(
    private userService: UserService,
    private profilService: ProfilService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadProfils();
  }

  // ---------- LOAD DATA ----------

  loadUsers(): void {
    this.loadingUsers = true;
    this.userError = undefined;

    this.userService.getAllUsers(this.search).subscribe({
      next: (data: User[]) => {
        this.users = data;
        this.loadingUsers = false;

        if (!this.selectedUser && this.users.length > 0) {
          this.onSelect(this.users[0]);
        }
      },
      error: (err: any) => {
        console.error(err);
        this.userError = 'Erreur lors du chargement des utilisateurs.';
        this.toastr.error(this.userError);
        this.loadingUsers = false;
      }
    });
  }

  loadProfils(): void {
    this.loadingProfils = true;
    this.profilService.getAll().subscribe({
      next: (profils: Profil[]) => {
        this.profils = profils;
        this.loadingProfils = false;
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des profils.');
        this.loadingProfils = false;
      }
    });
  }

  // ---------- SELECTION ----------

  onSelect(u: User): void {
    this.selectedUser = u;
    this.showForm = false;
    this.currentId = u.id;
    this.isEditing = false;
    this.activeTab = 'INFO';
  }

  // ---------- CRUD FORM ----------

  openCreateForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.currentId = undefined;
    this.form = {
      code: '',
      firstName: '',
      lastName: '',
      email: '',
      actif: true,
      profilId: undefined
    };
  }

  openEditForm(): void {
    if (!this.selectedUser) {
      return;
    }
    const u = this.selectedUser;
    this.showForm = true;
    this.isEditing = true;
    this.currentId = u.id;

    this.form = {
      code: u.code,
      firstName: u.firstName,
      lastName: u.lastName,
      email: u.email,
      actif: u.actif ?? true,
      profilId: u.profilId
    };
  }

  cancelForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.currentId = undefined;
  }

  submitForm(): void {
    if (!this.form.code || !this.form.firstName || !this.form.lastName || !this.form.email) {
      this.toastr.warning('Code, nom, prénom et email sont obligatoires.');
      return;
    }

    const payload: Partial<User> = {
      code: this.form.code,
      firstName: this.form.firstName,
      lastName: this.form.lastName,
      email: this.form.email,
      actif: this.form.actif ?? true,
      profilId: this.form.profilId
    };

    if (this.isEditing && this.currentId) {
      // 🔁 update
      this.userService.update(this.currentId, payload).subscribe({
        next: () => {
          this.toastr.success('Utilisateur mis à jour.');
          this.showForm = false;
          this.loadUsers();
        },
        error: (err: any) => {
          const msg = err?.error?.message || 'Erreur lors de la mise à jour.';
          this.toastr.error(msg);
        }
      });
    } else {
      // ➕ create
      this.userService.create(payload).subscribe({
        next: () => {
          this.toastr.success('Utilisateur créé.');
          this.showForm = false;
          this.loadUsers();
        },
        error: (err: any) => {
          const msg = err?.error?.message || 'Erreur lors de la création.';
          this.toastr.error(msg);
        }
      });
    }
  }

  deleteUser(u: User): void {
    if (!confirm(`Supprimer l'utilisateur ${u.code} (${u.email}) ?`)) {
      return;
    }

    this.userService.delete(u.id).subscribe({
      next: () => {
        this.toastr.success('Utilisateur supprimé.');
        if (this.selectedUser?.id === u.id) {
          this.selectedUser = undefined;
        }
        this.loadUsers();
      },
      error: (err: any) => {
        const msg = err?.error?.message || 'Erreur lors de la suppression.';
        this.toastr.error(msg);
      }
    });
  }

  // ---------- TABS ----------

  setActiveTab(tab: 'INFO' | 'PERMISSIONS' | 'ROLES'): void {
    this.activeTab = tab;
  }

  // Helpers
  getProfilLibelle(user: User): string {
    if (user.profilLibelle) {
      return user.profilLibelle;
    }
    const p = this.profils.find(pr => pr.id === user.profilId);
    return p ? p.libelle : '';
  }

  getInitials(user: User): string {
    const fn = (user.firstName || '').charAt(0);
    const ln = (user.lastName || '').charAt(0);
    return (fn + ln).toUpperCase();
  }
}
