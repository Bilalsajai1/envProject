import {Component, OnInit} from '@angular/core';
import {EnvApplication} from '../../models/env-application.model';
import {ActivatedRoute, Router} from '@angular/router';
import {EnvApplicationService} from '../../services/env-application-service';
import {ToastrService} from 'ngx-toastr';
import {BreadcrumbService} from '../../breadcrumb/breadcrumb.component';

@Component({
  selector: 'app-application-list',
  standalone: false,
  templateUrl: './application-list.component.html',
  styleUrl: './application-list.component.scss',
})
export class ApplicationListComponent implements OnInit {

  typeCode!: string;
  envId!: number;
  projectId!: number;

  applications: EnvApplication[] = [];
  loading = false;
  error?: string;

  showPassword = false;

  // formulaire
  showForm = false;
  isEditing = false;
  currentAppId?: number;

  form: Partial<EnvApplication> = {
    applicationId: undefined,
    protocole: '',
    host: '',
    port: undefined,
    url: '',
    username: '',
    password: '',
    description: '',
    actif: true
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: EnvApplicationService,
    private toastr: ToastrService,
    private breadcrumbService: BreadcrumbService
  ) {}

  ngOnInit(): void {
    this.typeCode = this.route.snapshot.paramMap.get('typeCode') || '';

    const envIdParam = this.route.snapshot.paramMap.get('envId');
    const projectIdParam = this.route.snapshot.paramMap.get('projectId');

    this.envId = envIdParam ? +envIdParam : 0;
    this.projectId = projectIdParam ? +projectIdParam : 0;

    this.breadcrumbService.setBreadcrumbs([
      { label: this.typeCode.toUpperCase(), url: `/environment/${this.typeCode}` },
      { label: 'Projets', url: `/environment/${this.typeCode}/projects` },
      { label: `Projet ${this.projectId}`, url: `/environment/${this.typeCode}/projects/${this.projectId}/environments` },
      { label: `Env ${this.envId}`, url: `/environment/${this.typeCode}/projects/${this.projectId}/environments/${this.envId}/applications` },
      { label: 'Applications', url: '' }
    ]);

    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.getApplicationsForEnv(this.envId).subscribe({
      next: apps => {
        this.applications = apps;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur chargement applications.';
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate([
      '/environment',
      this.typeCode,
      'projects',
      this.projectId,
      'environments'
    ]);
  }

  // ---- Password display ----

  toggleShowPassword(): void {
    this.showPassword = !this.showPassword;
  }

  getPasswordToDisplay(app: EnvApplication): string {
    if (this.showPassword) {
      return app.password ?? app.passwordMasked ?? '';
    }
    return '********';
  }

  // ---- FORM CRUD ----

  openCreateForm(): void {
    this.isEditing = false;
    this.currentAppId = undefined;
    this.form = {
      applicationId: undefined,
      protocole: '',
      host: '',
      port: undefined,
      url: '',
      username: '',
      password: '',
      description: '',
      actif: true
    };
    this.showForm = true;
  }

  openEditForm(app: EnvApplication): void {
    this.isEditing = true;
    this.currentAppId = app.id;

    this.form = {
      applicationId: app.applicationId,
      protocole: app.protocole,
      host: app.host,
      port: app.port,
      url: app.url,
      username: app.username,
      password: app.password, // ou rien si tu ne veux pas recharger
      description: app.description,
      actif: app.actif
    };

    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.currentAppId = undefined;
  }


  submitForm(): void {
    if (!this.form.applicationId) {
      this.toastr.warning('L’application est obligatoire.');
      return;
    }

    const payload: Partial<EnvApplication> = {
      environnementId: this.envId,
      applicationId: this.form.applicationId,
      protocole: this.form.protocole,
      host: this.form.host,
      port: this.form.port,
      url: this.form.url,
      username: this.form.username,
      password: this.form.password,
      description: this.form.description,
      actif: this.form.actif
    };

    if (this.isEditing && this.currentAppId) {
      this.service.updateApplication(this.currentAppId, payload).subscribe({
        next: () => {
          this.toastr.success('Configuration mise à jour.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la mise à jour.';
          this.toastr.error(msg);
        }
      });
    } else {
      this.service.createApplication(payload).subscribe({
        next: () => {
          this.toastr.success('Configuration créée.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          // Ici on récupère le message d’IllegalStateException si app déjà présente
          const msg = err.error?.message || 'Erreur lors de la création.';
          this.toastr.error(msg);
        }
      });
    }
  }

  deleteApp(app: EnvApplication): void {
    if (!confirm(`Supprimer l'application ${app.applicationCode} ?`)) {
      return;
    }
    this.service.deleteApplication(app.id).subscribe({
      next: () => {
        this.toastr.success('Configuration supprimée.');
        this.load();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression.';
        this.toastr.error(msg);
      }
    });
  }
}
