import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

import { User } from '../../environement/models/user.model';
import { UserService } from '../../environement/services/user-service';
import { ProfilService } from '../../environement/services/profil-service';
import { RoleService } from '../../environement/services/role-service';
import { MenuService } from '../../environement/services/menu-service';

import { Role } from '../../environement/models/role.model';
import { Menu } from '../../environement/models/menu.model';
import { PageResponse } from '../../environement/services/user-service';

type ActionType = 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE';

interface ActionRoleView {
  action: ActionType;
  roleId: number;
}

interface EnvUnit {
  key: string;
  label: string;
  menu?: Menu;
  environnementCode?: string | null;
  actions: ActionRoleView[];
}

interface EnvTypeBlock {
  envTypeCode: string;    // e.g. EDITION / INT / PROD
  envTypeLabel: string;
  units: EnvUnit[];
}

@Component({
  selector: 'app-role-admin',
  standalone: false,
  templateUrl: './role-admin.component.html',
  styleUrls: ['./role-admin.component.scss']
})
export class RoleAdminComponent implements OnInit {

  // ---------- USERS ----------
  users: User[] = [];
  loadingUsers = false;
  userSearch = '';
  selectedUser?: User;

  pageIndex = 0;
  pageSize = 20;
  totalElements = 0;

  // ---------- ROLES / MENUS ----------
  allRoles: Role[] = [];
  allMenus: Menu[] = [];

  // structure pour UI (groupée par type d'env)
  envBlocks: EnvTypeBlock[] = [];

  // roles actuellement assignés au profil sélectionné
  selectedRoleIds = new Set<number>();

  loadingPermissions = false;
  saving = false;

  constructor(
    private userService: UserService,
    private profilService: ProfilService,
    private roleService: RoleService,
    private menuService: MenuService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadRolesAndMenus();
  }

  // ---------------- USERS ----------------

  loadUsers(): void {
    this.loadingUsers = true;

    this.userService.searchUsers({
      page: this.pageIndex,
      size: this.pageSize,
      sort: 'code,asc',
      text: this.userSearch.trim() || undefined
    }).subscribe({
      next: (page: PageResponse<User>) => {
        this.users = page.content;
        this.totalElements = page.totalElements;
        this.loadingUsers = false;

        if (!this.selectedUser && this.users.length > 0) {
          this.onSelectUser(this.users[0]);
        }
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des utilisateurs.');
        this.loadingUsers = false;
      }
    });
  }

  onUserSearchChange(value: string): void {
    this.userSearch = value;
    this.pageIndex = 0;
    this.loadUsers();
  }

  onUserPageChange(pageIndex: number, pageSize: number): void {
    this.pageIndex = pageIndex;
    this.pageSize = pageSize;
    this.loadUsers();
  }

  onSelectUser(user: User): void {
    this.selectedUser = user;
    this.loadProfilRolesForUser(user);
  }

  private loadProfilRolesForUser(user: User): void {
    if (!user.profilId) {
      this.toastr.warning('Cet utilisateur n’a pas de profil associé.');
      this.selectedRoleIds = new Set<number>();
      return;
    }

    this.loadingPermissions = true;

    this.profilService.getRolesByProfil(user.profilId).subscribe({
      next: (roleIds: number[]) => {
        this.selectedRoleIds = new Set(roleIds);
        this.loadingPermissions = false;
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des rôles du profil.');
        this.loadingPermissions = false;
      }
    });
  }

  // ---------------- ROLES / MENUS -> STRUCTURE UI ----------------

  private loadRolesAndMenus(): void {
    // charger les rôles
    this.roleService.getAll().subscribe({
      next: (roles: Role[]) => {
        this.allRoles = roles;
        this.buildEnvBlocks();
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des rôles.');
      }
    });

    // charger les menus
    this.menuService.getAll().subscribe({
      next: (menus: Menu[]) => {
        this.allMenus = menus;
        this.buildEnvBlocks();
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des menus.');
      }
    });
  }

  private buildEnvBlocks(): void {
    if (!this.allRoles.length || !this.allMenus.length) {
      return;
    }

    const menuById = new Map<number, Menu>();
    this.allMenus.forEach(m => menuById.set(m.id, m));

    const blocksMap = new Map<string, EnvTypeBlock>();

    for (const role of this.allRoles) {
      const menu = role.menuId ? menuById.get(role.menuId) : undefined;
      const envTypeCode = menu?.environmentTypeCode || 'GLOBAL';

      let block = blocksMap.get(envTypeCode);
      if (!block) {
        block = {
          envTypeCode,
          envTypeLabel: menu?.environmentTypeCode || envTypeCode,
          units: []
        };
        blocksMap.set(envTypeCode, block);
      }

      // clé d’un "module" (menu + éventuellement env)
      const unitKey = `${menu?.id || 'nomenu'}_${role.environnementId || 'noenv'}`;

      let unit = block.units.find(u => u.key === unitKey);
      if (!unit) {
        const label =
          (menu?.libelle || menu?.code) ??
          role.environnementCode ??
          'Global';

        unit = {
          key: unitKey,
          label,
          menu,
          environnementCode: role.environnementCode,
          actions: []
        };
        block.units.push(unit);
      }

      // ajouter l’action correspondante
      const action: ActionType = role.action as ActionType;

      // éviter doublon d’action
      if (!unit.actions.find(a => a.action === action)) {
        unit.actions.push({
          action,
          roleId: role.id
        });
      }
    }

    // tri par code
    this.envBlocks = Array.from(blocksMap.values())
      .sort((a, b) => a.envTypeCode.localeCompare(b.envTypeCode))
      .map(block => ({
        ...block,
        units: block.units.sort((u1, u2) => u1.label.localeCompare(u2.label))
      }));
  }

  // ---------------- GESTION DES CHECKBOX ----------------

  isRoleChecked(roleId: number): boolean {
    return this.selectedRoleIds.has(roleId);
  }

  toggleRole(roleId: number): void {
    if (this.selectedRoleIds.has(roleId)) {
      this.selectedRoleIds.delete(roleId);
    } else {
      this.selectedRoleIds.add(roleId);
    }
  }

  // cocher / décocher toutes les actions d’un unit
  toggleUnit(unit: EnvUnit, checked: boolean): void {
    for (const a of unit.actions) {
      if (checked) {
        this.selectedRoleIds.add(a.roleId);
      } else {
        this.selectedRoleIds.delete(a.roleId);
      }
    }
  }

  unitHasAllActions(unit: EnvUnit): boolean {
    return unit.actions.every(a => this.selectedRoleIds.has(a.roleId));
  }

  // ---------------- SAVE ----------------

  savePermissions(): void {
    if (!this.selectedUser?.profilId) {
      this.toastr.error('Utilisateur sans profil, impossible de sauvegarder.');
      return;
    }

    const roleIds = Array.from(this.selectedRoleIds);

    this.saving = true;
    this.profilService.updateProfilRoles(this.selectedUser.profilId, roleIds).subscribe({
      next: () => {
        this.toastr.success('Permissions mises à jour avec succès.');
        this.saving = false;
      },
      error: (err: any) => {
        console.error(err);
        const msg = err.error?.message || 'Erreur lors de la sauvegarde des permissions.';
        this.toastr.error(msg);
        this.saving = false;
      }
    });
  }

  // Helpers affichage
  getUserDisplay(u: User): string {
    return `${u.lastName || ''} ${u.firstName || ''}`.trim() || u.code;
  }

  getUserProfilLabel(u: User): string {
    return u.profilLibelle || u.profilCode || '';
  }

  getEnvTypeLabel(block: EnvTypeBlock): string {
    // tu peux mapper EDITION/INT/PROD → label plus joli ici
    return block.envTypeCode;
  }
}
