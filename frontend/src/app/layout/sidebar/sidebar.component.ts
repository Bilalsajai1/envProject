import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthService} from '../../auth/auth-service';

interface SidebarItem {
  label: string;
  route: string;
  icon?: string;
}

interface SidebarSection {
  title: string;
  icon?: string;
  collapsed: boolean;
  items: SidebarItem[];
}

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  @Input() isSidebarCollapsed = false;
  @Output() sidebarToggle = new EventEmitter<void>();

  sections: SidebarSection[] = [];

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    this.loadSidebar();
  }

  /** Charge les menus depuis le backend */
  private loadSidebar(): void {
    this.auth.auth$.subscribe(ctx => {
      if (!ctx) return;

      const menus = ctx.menus.sort((a, b) => a.ordre - b.ordre);

      // SECTION : Dashboard toujours présent
      const dashboardSection: SidebarSection = {
        title: 'Vue globale',
        icon: 'fas fa-chart-pie',
        collapsed: false,
        items: [
          { label: 'Dashboard', route: '/dashboard', icon: 'fas fa-home' }
        ]
      };

      // SECTION : Environnements
      const envItems = menus
        .filter(m => m.route === '/edition' || m.route === '/integration' || m.route === '/client')
        .map(m => ({
          label: m.libelle,
          route: '/environment',
          icon: 'fas fa-layer-group'
        }));

      const envSection: SidebarSection = {
        title: 'Environnements',
        icon: 'fas fa-globe',
        collapsed: false,
        items: envItems
      };

      // SECTION : Administration (menus /admin/**)
      const adminItems = menus
        .filter(m => m.route.startsWith('/admin'))
        .map(m => ({
          label: m.libelle,
          route: m.route,
          icon: 'fas fa-project-diagram'
        }));

      const adminSection: SidebarSection = {
        title: 'Administration',
        icon: 'fas fa-user-shield',
        collapsed: false,
        items: adminItems
      };

      this.sections = [
        dashboardSection,
        ...(envItems.length > 0 ? [envSection] : []),
        ...(adminItems.length > 0 ? [adminSection] : [])
      ];
    });
  }

  toggleSection(index: number): void {
    this.sections[index].collapsed = !this.sections[index].collapsed;
  }
}
