import {Component, Injectable, OnInit} from '@angular/core';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { filter } from 'rxjs/operators';

interface BreadcrumbItem {
  label: string;
  url: string;
}

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {
  private breadcrumbs: BreadcrumbItem[] = [];

  setBreadcrumbs(items: BreadcrumbItem[]): void {
    this.breadcrumbs = items;
  }

  getBreadcrumbs(): BreadcrumbItem[] {
    return this.breadcrumbs;
  }
}

@Component({
  selector: 'app-breadcrumb',
  standalone: false,
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss'
})
export class BreadcrumbComponent implements OnInit {

  breadcrumbs: BreadcrumbItem[] = [];

  constructor(
    private router: Router,
    private breadcrumbService: BreadcrumbService
  ) {}

  ngOnInit(): void {
    // Toujours avoir Dashboard au début
    this.breadcrumbs = [
      { label: 'Dashboard', url: '/dashboard' }
    ];

    // Écouter les changements de route
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        const additionalBreadcrumbs = this.breadcrumbService.getBreadcrumbs();
        this.breadcrumbs = [
          { label: 'Dashboard', url: '/dashboard' },
          ...additionalBreadcrumbs
        ];
      });
  }

  navigate(url: string): void {
    this.router.navigate([url]);
  }
}
