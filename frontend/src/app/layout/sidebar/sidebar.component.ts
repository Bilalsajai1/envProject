import { Component, EventEmitter, Input, Output } from '@angular/core';

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
export class SidebarComponent {
  @Input() isSidebarCollapsed = false;
  @Output() sidebarToggle = new EventEmitter<void>();

  sections: SidebarSection[] = [
    {
      title: 'Vue globale',
      icon: 'fas fa-chart-pie',
      collapsed: false,
      items: [
        { label: 'Dashboard', route: '/dashboard', icon: 'fas fa-home' }
      ]
    },
    {
      title: 'Environnements',
      icon: 'fas fa-globe',
      collapsed: false,
      items: [
        { label: 'Types d\'environnement', route: '/environment', icon: 'fas fa-layer-group' }
      ]
    },
    {
      title: 'Administration',
      icon: 'fas fa-user-shield',
      collapsed: false,
      items: [
        { label: 'Gestion des projets', route: '/admin/projects', icon: 'fas fa-project-diagram' },
        { label: 'Gestion des applications', route: '/admin/applications', icon: 'fas fa-th-large' }
      ]
    }
  ];


  toggleSection(index: number): void {
    this.sections[index].collapsed = !this.sections[index].collapsed;
  }
}
