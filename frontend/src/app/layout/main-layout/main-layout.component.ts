import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { Router } from '@angular/router';
import { AuthContextService } from '../../auth/services/auth-context.service';
import { AuthenticationService } from '../../auth/services/authentication.service';
import { AuthContext } from '../../auth/models/auth-context.model';
import { Subject, takeUntil } from 'rxjs';
import { MatSidenavContainer } from '@angular/material/sidenav';

interface AdminNavItem {
  label: string;
  icon?: string;
  route: string;
  requiredRoles?: string[];
}

interface EnvNavItem {
  label: string;
  route: string;
  code: string;
  projectCount: number;
}

@Component({
  selector: 'app-main-layout',
  standalone: false,
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  @ViewChild(MatSidenavContainer) sidenavContainer?: MatSidenavContainer;

  context: AuthContext | null = null;

  isSidebarCollapsed = false;
  isDarkMode = false;

  private readonly SIDEBAR_KEY = 'perenity.sidebar';
  private readonly THEME_KEY = 'perenity.theme';
  private readonly destroy$ = new Subject<void>();

  readonly adminMenu: AdminNavItem[] = [
    { label: 'Utilisateurs', icon: 'group', route: '/admin/users', requiredRoles: ['ADMIN', 'ROLE_USERS_ACCESS'] },
    { label: 'Profils', icon: 'badge', route: '/admin/profils', requiredRoles: ['ADMIN'] },
    { label: 'Permissions', icon: 'admin_panel_settings', route: '/admin/permissions', requiredRoles: ['ADMIN', 'ROLE_ROLES_EDIT'] }
  ];

  envMenu: EnvNavItem[] = [];
  private readonly ENV_ORDER = ['EDITION', 'INTEGRATION', 'CLIENT'];

  constructor(
    private readonly authCtx: AuthContextService,
    private readonly auth: AuthenticationService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}
  get hasAdminMenu(): boolean {
    return this.visibleAdminMenu.length > 0;
  }

  get hasEnvMenu(): boolean {
    return this.envMenu.length > 0;
  }

  ngOnInit(): void {
    this.isSidebarCollapsed = localStorage.getItem(this.SIDEBAR_KEY) === 'true';
    this.isDarkMode = localStorage.getItem(this.THEME_KEY) === 'dark';

    document.documentElement.setAttribute(
      'data-theme',
      this.isDarkMode ? 'dark' : 'light'
    );

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

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
    localStorage.setItem(this.SIDEBAR_KEY, String(this.isSidebarCollapsed));
    this.sidenavContainer?.updateContentMargins();
    this.cdr.markForCheck();
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;

    localStorage.setItem(this.THEME_KEY, this.isDarkMode ? 'dark' : 'light');

    document.documentElement.setAttribute(
      'data-theme',
      this.isDarkMode ? 'dark' : 'light'
    );

    this.cdr.markForCheck();
  }

  /* -----------------------------------------------
     ENV MENU BUILDING
  ------------------------------------------------ */
  private buildEnvMenu(): void {
    if (!this.context?.environmentTypes) {
      this.envMenu = [];
      return;
    }

    const isAdmin = this.context.user?.admin ?? false;

    const mapped = this.context.environmentTypes
      .filter(env =>
        isAdmin || (env.projects ?? []).some(p => p.allowedActions?.includes('CONSULT'))
      )
      .map(env => ({
        label: env.libelle,
        route: `/env/${env.code.toLowerCase()}`,
        code: env.code,
        projectCount: isAdmin
          ? (env.projects ?? []).length
          : (env.projects ?? []).filter(p => p.allowedActions?.includes('CONSULT')).length
      }));

    this.envMenu = mapped.sort((a, b) => {
      const ai = this.ENV_ORDER.indexOf(a.code);
      const bi = this.ENV_ORDER.indexOf(b.code);
      if (ai !== -1 && bi !== -1) return ai - bi;
      return a.label.localeCompare(b.label);
    });
  }

  /* -----------------------------------------------
     USER + ROLE HELPERS
  ------------------------------------------------ */
  isAdmin(): boolean {
    return this.context?.user?.admin ?? false;
  }

  hasRole(role: string): boolean {
    return this.context?.user?.roles?.includes(role) ?? false;
  }

  hasAnyRole(required?: string[]): boolean {
    if (this.isAdmin()) return true;
    if (!required?.length) return true;
    return required.some(r => this.hasRole(r));
  }

  get visibleAdminMenu(): AdminNavItem[] {
    return this.adminMenu.filter(i => this.hasAnyRole(i.requiredRoles));
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
  }

  getUserInitials(): string {
    const u = this.context?.user;
    return ((u?.firstName?.[0] ?? '') + (u?.lastName?.[0] ?? '')).toUpperCase() || '??';
  }

  get userDisplayName(): string {
    const u = this.context?.user;
    return `${u?.firstName ?? ''} ${u?.lastName ?? ''}`.trim();
  }

  get userProfilLabel(): string {
    return this.context?.user?.profilLibelle ?? '';
  }

  get userEmail(): string {
    return this.context?.user?.email ?? '';
  }
}
