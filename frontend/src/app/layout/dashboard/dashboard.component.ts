import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import {DashboardSummary} from '../../environement/models/dashboard-summary.model';
import {DashboardService} from '../dashboard-service';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  summary?: DashboardSummary;
  loading = false;
  error?: string;

  constructor(
    private dashboardService: DashboardService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();

    // Chaque fois qu'on revient sur /dashboard, on recharge les stats
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        if (e.urlAfterRedirects.startsWith('/dashboard')) {
          this.load();
        }
      });
  }

  load(): void {
    this.loading = true;
    this.dashboardService.getSummary().subscribe({
      next: (s) => {
        this.summary = s;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement du dashboard.';
        this.loading = false;
      }
    });
  }
}
