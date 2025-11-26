import {Component, OnInit} from '@angular/core';
import {Projet} from '../../models/projet.model';
import {ActivatedRoute, Router} from '@angular/router';
import {ProjetService} from '../../services/projet-service';
import {BreadcrumbService} from '../../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-project-list',
  standalone: false,
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.scss',
})
export class ProjectListComponent implements OnInit {

  typeCode!: string;
  projects: Projet[] = [];
  loading = false;
  error?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetService: ProjetService ,
    private breadcrumbService: BreadcrumbService
  ) {}

  ngOnInit(): void {
    this.typeCode = this.route.snapshot.paramMap.get('typeCode') || '';

    this.breadcrumbService.setBreadcrumbs([
      { label: this.typeCode.toUpperCase(), url: `/environment/${this.typeCode}` },
      { label: 'Projets', url: `/environment/${this.typeCode}/projects` }
    ]);

    this.loadProjects();
  }

  loadProjects(): void {
    if (!this.typeCode) {
      this.error = 'Type d’environnement manquant.';
      return;
    }

    this.loading = true;
    this.error = undefined;

    this.projetService.getProjectsByEnvironmentType(this.typeCode).subscribe({
      next: projects => {
        this.projects = projects;
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.error = 'Erreur lors du chargement des projets.';
        this.loading = false;
      }
    });
  }

  goBackToTypes(): void {
    this.router.navigate(['/environment']);
  }

  goToEnvironments(projet: Projet): void {
    this.router.navigate([
      '/environment',
      this.typeCode,
      'projects',
      projet.id,
      'environments'
    ]);
  }
}
