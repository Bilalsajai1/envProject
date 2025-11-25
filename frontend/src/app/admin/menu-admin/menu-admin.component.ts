// src/app/admin/menu-admin/menu-admin.component.ts
import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { MenuAdmin } from '../../environement/models/menu-admin.model';
import { Role } from '../../environement/models/role.model';
import { MenuAdminService } from '../../environement/services/menu-admin.service';
import {RoleService} from '../../environement/services/role-service';

@Component({
  selector: 'app-menu-admin',
  standalone: false,
  templateUrl: './menu-admin.component.html',
  styleUrls: ['./menu-admin.component.scss']
})
export class MenuAdminComponent implements OnInit {

  menus: MenuAdmin[] = [];
  loading = false;
  error?: string;

  // Formulaire création / édition
  showForm = false;
  isEditing = false;
  currentId?: number;

  form: Partial<MenuAdmin> = {
    code: '',
    libelle: '',
    route: '',
    icon: '',
    ordre: 0,
    visible: true
  };

  // Gestion des rôles d’un menu
  allRoles: Role[] = [];
  showRolesPanel = false;
  currentMenuForRoles?: MenuAdmin;
  selectedRoleIds = new Set<number>();
  loadingRoles = false;

  constructor(
    private menuAdminService: MenuAdminService,
    private roleService: RoleService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadMenus();
    this.loadAllRoles();
  }

  // --------- MENUS ---------

  loadMenus(): void {
    this.loading = true;
    this.error = undefined;

    this.menuAdminService.getAllMenus().subscribe({
      next: (data) => {
        this.menus = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Erreur lors du chargement des menus.';
        this.toastr.error(this.error);
        this.loading = false;
      }
    });
  }

  openCreateForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.currentId = undefined;
    this.form = {
      code: '',
      libelle: '',
      route: '',
      icon: '',
      ordre: 0,
      visible: true
    };
  }

  openEditForm(m: MenuAdmin): void {
    this.showForm = true;
    this.isEditing = true;
    this.currentId = m.id;
    this.form = { ...m };
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

    const payload: Partial<MenuAdmin> = {
      code: this.form.code,
      libelle: this.form.libelle,
      route: this.form.route,
      icon: this.form.icon,
      ordre: this.form.ordre,
      visible: this.form.visible
      // parentId, environmentTypeId pour plus tard si on veut
    };

    if (this.isEditing && this.currentId) {
      this.menuAdminService.updateMenu(this.currentId, payload).subscribe({
        next: () => {
          this.toastr.success('Menu mis à jour avec succès.');
          this.showForm = false;
          this.loadMenus();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la mise à jour du menu.';
          this.toastr.error(msg);
        }
      });
    } else {
      this.menuAdminService.createMenu(payload).subscribe({
        next: () => {
          this.toastr.success('Menu créé avec succès.');
          this.showForm = false;
          this.loadMenus();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la création du menu.';
          this.toastr.error(msg);
        }
      });
    }
  }

  deleteMenu(m: MenuAdmin): void {
    if (!m.id) { return; }

    if (!confirm(`Supprimer le menu ${m.code} ?`)) {
      return;
    }

    this.menuAdminService.deleteMenu(m.id).subscribe({
      next: () => {
        this.toastr.success('Menu supprimé avec succès.');
        this.loadMenus();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression du menu.';
        this.toastr.error(msg);
      }
    });
  }

  // --------- RÔLES D’UN MENU ---------

  loadAllRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (roles) => {
        this.allRoles = roles;
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des rôles.');
      }
    });
  }

  openRolesPanel(m: MenuAdmin): void {
    if (!m.id) { return; }

    this.currentMenuForRoles = m;
    this.selectedRoleIds.clear();
    this.showRolesPanel = true;
    this.loadingRoles = true;

    this.menuAdminService.getRolesForMenu(m.id).subscribe({
      next: (roles) => {
        roles.forEach(r => this.selectedRoleIds.add(r.id));
        this.loadingRoles = false;
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des rôles du menu.');
        this.loadingRoles = false;
      }
    });
  }

  closeRolesPanel(): void {
    this.showRolesPanel = false;
    this.currentMenuForRoles = undefined;
    this.selectedRoleIds.clear();
  }

  toggleRole(roleId: number): void {
    if (this.selectedRoleIds.has(roleId)) {
      this.selectedRoleIds.delete(roleId);
    } else {
      this.selectedRoleIds.add(roleId);
    }
  }

  saveRoles(): void {
    if (!this.currentMenuForRoles?.id) {
      return;
    }

    const menuId = this.currentMenuForRoles.id;
    const roleIds = Array.from(this.selectedRoleIds);

    this.menuAdminService.updateRolesForMenu(menuId, roleIds).subscribe({
      next: () => {
        this.toastr.success('Rôles du menu mis à jour.');
        this.closeRolesPanel();
      },
      error: (err) => {
        console.error(err);
        const msg = err.error?.message || 'Erreur lors de la mise à jour des rôles du menu.';
        this.toastr.error(msg);
      }
    });
  }

  // Helpers affichage action
  getRoleActionLabel(action?: string): string {
    switch (action) {
      case 'CONSULT': return 'Consultation';
      case 'CREATE': return 'Création';
      case 'UPDATE': return 'Modification';
      case 'DELETE': return 'Suppression';
      default: return action || '';
    }
  }
}
