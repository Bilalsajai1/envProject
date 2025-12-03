// src/app/layout/main-layout/main-layout.component.ts

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit ,
  ViewChild
} from '@angular/core';
import { Router } from '@angular/router';
import { AuthContextService } from '../../auth/services/auth-context.service';
import { AuthenticationService } from '../../auth/services/authentication.service';
import { AuthContext } from '../../auth/models/auth-context.model';
import { Subject, takeUntil } from 'rxjs';
import { MatSidenavContainer } from '@angular/material/sidenav'

interface AdminNavItem {
  label: string;
  icon?: string;
  route: string;
  requiredRoles?: string[];
}

interface EnvNavItem {
  label: string;
  route: string;
  env: any;
}

@Component({
  selector: 'app-main-layout',
  standalone: false,
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  @ViewChild(MatSidenavContainer)
  sidenavContainer?: MatSidenavContainer;

  context: AuthContext | null = null;

  // État du sidebar et du thème
  isSidebarCollapsed = false;
  isDarkMode = false;

  // Storage keys
  private readonly SIDEBAR_STATE_KEY = 'perenity-sidebar-collapsed';
  private readonly THEME_KEY = 'perenity-theme';

  // Menu Administration
  adminMenu: AdminNavItem[] = [
    {
      label: 'Utilisateurs',
      icon: 'group',
      route: '/admin/users',
      requiredRoles: ['ADMIN', 'ROLE_USERS_ACCESS']
    },
    {
      label: 'Profils',
      icon: 'badge',
      route: '/admin/profils',
      requiredRoles: ['ADMIN']
    },
    {
      label: 'Permissions',
      icon: 'admin_panel_settings',
      route: '/admin/permissions',
      requiredRoles: ['ADMIN', 'ROLE_ROLES_EDIT']
    }
  ];

  // Menu Environnements
  envMenu: EnvNavItem[] = [];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly authCtx: AuthContextService,
    private readonly auth: AuthenticationService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadSavedStates();
  

    this.authCtx.getContext$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(ctx => {
        this.context = ctx ?? null;
        this.buildEnvMenu();
        this.cdr.markForCheck();
      });
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadSavedStates(): void {
    const savedSidebarState = localStorage.getItem(this.SIDEBAR_STATE_KEY);
    if (savedSidebarState !== null) {
      this.isSidebarCollapsed = savedSidebarState === 'true';
    }

    const savedTheme = localStorage.getItem(this.THEME_KEY);
    if (savedTheme !== null) {
      this.isDarkMode = savedTheme === 'dark';
    }
  }
  private saveSidebarState(): void {
    localStorage.setItem(this.SIDEBAR_STATE_KEY, this.isSidebarCollapsed.toString());
  }

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
    this.saveSidebarState();

    // force Angular Material à recalculer les marges du contenu
    setTimeout(() => {
      this.sidenavContainer?.updateContentMargins();
    }, 0);

    this.cdr.markForCheck();
  }

  private buildEnvMenu(): void {
    const ctx = this.context;
    if (!ctx || !ctx.environmentTypes) {
      this.envMenu = [];
      return;
    }

    this.envMenu = ctx.environmentTypes
      .filter(t => t.allowedActions && t.allowedActions.includes('CONSULT'))
      .map(t => ({
        label: t.libelle,
        route: `/env/${t.code.toLowerCase()}`,
        env: t
      }))
      .sort((a, b) => a.label.localeCompare(b.label));
  }

  isAdmin(): boolean {
    return this.context?.user?.admin ?? false;
  }

  hasRole(role: string): boolean {
    const roles = this.context?.user?.roles ?? [];
    return roles.includes(role);
  }

  hasAnyRole(required?: string[]): boolean {
    if (this.isAdmin()) return true;
    if (!required || required.length === 0) return true;
    return required.some(r => this.hasRole(r));
  }

  shouldShowMenuItem(item: AdminNavItem): boolean {
    return this.hasAnyRole(item.requiredRoles);
  }

  get visibleAdminMenu(): AdminNavItem[] {
    return this.adminMenu.filter(item => this.shouldShowMenuItem(item));
  }

  get hasEnvMenu(): boolean {
    return this.envMenu.length > 0;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
  }

  getUserInitials(): string {
    const user = this.context?.user;
    if (!user) return '??';
    const first = user.firstName?.[0] || '';
    const last = user.lastName?.[0] || '';
    return (first + last).toUpperCase();
  }

  get userDisplayName(): string {
    const user = this.context?.user;
    if (!user) return '';
    return `${user.firstName} ${user.lastName}`.trim();
  }

  get userProfilLabel(): string {
    return this.context?.user?.profilLibelle ?? '';
  }

  get userEmail(): string {
    return this.context?.user?.email ?? '';
  }
}
