// src/app/layout/main-layout/main-layout.component.ts

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthContextService } from '../../auth/services/auth-context.service';
import { AuthenticationService } from '../../auth/services/authentication.service';
import { AuthContext } from '../../auth/models/auth-context.model';

interface AdminNavItem {
  label: string;
  icon?: string;
  route: string;
  requiredRoles?: string[];
}

interface EnvNavItem {
  label: string;
  route: string;
  env: any; // ou EnvironmentTypePermission si tu veux
}

@Component({
  selector: 'app-main-layout',
  standalone: false,
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss']
})
export class MainLayoutComponent implements OnInit {

  context: AuthContext | null = null;

  // 1er menu : Administration
  adminMenu: AdminNavItem[] = [
    {
      label: 'Utilisateurs',
      icon: 'group',
      route: '/admin/users',
      requiredRoles: ['ADMIN', 'ROLE_USERS_ACCESS']
    },
    {
      label: 'Permissions',
      icon: 'admin_panel_settings',
      route: '/admin/permissions',
      requiredRoles: ['ADMIN', 'ROLE_ROLES_EDIT']
    },
    {
      label: 'Rôles',
      icon: 'shield',
      route: '/admin/roles',
      requiredRoles: ['ADMIN', 'ROLE_ROLES_ACCESS']
    }
  ];

  // 2ème menu : Environnements
  envMenu: EnvNavItem[] = [];

  constructor(
    private authCtx: AuthContextService,
    private auth: AuthenticationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authCtx.getContext$().subscribe(ctx => {
      this.context = ctx ?? null;
      this.buildEnvMenu();
    });
  }

  private buildEnvMenu(): void {
    const ctx = this.context;
    if (!ctx) {
      this.envMenu = [];
      return;
    }

    this.envMenu = ctx.environmentTypes
      .filter(t => t.allowedActions && t.allowedActions.includes('CONSULT'))
      .map(t => ({
        label: t.libelle,
        route: `/env/${t.code.toLowerCase()}`,
        env: t
      }));
  }

  // Vérifier si l'utilisateur est admin
  isAdmin(): boolean {
    return this.context?.user?.admin ?? false;
  }

  // Vérifier si l'utilisateur a un rôle spécifique
  hasRole(role: string): boolean {
    const roles = this.context?.user?.roles ?? [];
    return roles.includes(role);
  }

  // Vérifier si l'utilisateur a au moins un des rôles requis
  hasAnyRole(required?: string[]): boolean {
    if (!required || required.length === 0) {
      return true;
    }
    return required.some(r => this.hasRole(r));
  }

  // Vérifier si un item du menu doit être affiché
  shouldShowMenuItem(item: AdminNavItem): boolean {
    if (this.isAdmin()) {
      return true;
    }
    return this.hasAnyRole(item.requiredRoles);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
  }

  getUserInitials(): string {
    if (!this.context?.user) {
      return '??';
    }
    const first = this.context.user.firstName?.[0] || '';
    const last = this.context.user.lastName?.[0] || '';
    return (first + last).toUpperCase();
  }
}
