import {Component, OnInit} from '@angular/core';
import {Environnement} from '../../models/environnement.model';
import {EnvironmentType} from '../../models/environment-type.model';
import {ActivatedRoute, Router} from '@angular/router';
import {EnvironnementService} from '../../services/environement-service';
import {EnvironmentTypeService} from '../../services/environnement-type-service';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-environement-list',
  standalone: false,
  templateUrl: './environement-list.component.html',
  styleUrl: './environement-list.component.scss',
})
export class EnvironmentListComponent implements OnInit {

  typeCode!: string;
  projectId!: number;

  environments: Environnement[] = [];

  loading = false;
  error?: string;

  // pour création / édition
  showForm = false;
  isEditing = false;
  currentEnvId?: number;

  form: Partial<Environnement> = {
    code: '',
    libelle: '',
    description: '',
    actif: true
  };

  // pour récupérer typeId
  environmentTypes: EnvironmentType[] = [];
  typeId?: number;

  constructor(
    private environnementService: EnvironnementService,
    private environmentTypeService: EnvironmentTypeService,
    private route: ActivatedRoute,
    private router: Router,
    private toastr: ToastrService
  ) {}


  ngOnInit(): void {
    this.typeCode = this.route.snapshot.paramMap.get('typeCode') || '';

    const projectIdParam = this.route.snapshot.paramMap.get('projectId');
    this.projectId = projectIdParam ? +projectIdParam : 0;

    this.loadEnvironmentTypesAndData();
  }

  loadEnvironmentTypesAndData(): void {
    this.environmentTypeService.getAllTypes().subscribe({
      next: (types) => {
        this.environmentTypes = types;
        const match = types.find(t => t.code === this.typeCode);
        this.typeId = match?.id;
        this.loadEnvironments();
      },
      error: () => {
        this.error = 'Erreur lors du chargement des types.';
      }
    });
  }

  loadEnvironments(): void {
    if (!this.projectId || !this.typeCode) {
      this.error = 'Projet ou type manquant.';
      return;
    }

    this.loading = true;
    this.environnementService
      .getEnvironmentsByProjetAndType(this.projectId, this.typeCode)
      .subscribe({
        next: (envs) => {
          this.environments = envs;
          this.loading = false;
        },
        error: () => {
          this.error = 'Erreur lors du chargement des environnements.';
          this.loading = false;
        }
      });
  }

  goBack(): void {
    this.router.navigate(['/environment', this.typeCode, 'projects']);
  }

  // ---- FORM CRUD ----

  openCreateForm(): void {
    this.isEditing = false;
    this.currentEnvId = undefined;
    this.form = {
      code: '',
      libelle: '',
      description: '',
      actif: true
    };
    this.showForm = true;
  }

  openEditForm(env: Environnement): void {
    this.isEditing = true;
    this.currentEnvId = env.id;
    this.form = {
      code: env.code,
      libelle: env.libelle,
      description: env.description,
      actif: env.actif
    };
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.currentEnvId = undefined;
  }

  submitForm(): void {
    if (!this.form.code || !this.form.libelle) {
      this.toastr.warning('Code et libellé sont obligatoires.');
      return;
    }

    if (this.isEditing) {
      if (!this.currentEnvId) {
        return;
      }
      this.environnementService
        .updateEnvironnement(this.currentEnvId, {
          code: this.form.code,
          libelle: this.form.libelle,
          description: this.form.description,
          actif: this.form.actif
        })
        .subscribe({
          next: () => {
            this.toastr.success('Environnement mis à jour.');
            this.showForm = false;
            this.loadEnvironments();
          },
          error: (err) => {
            const msg = err.error?.message || 'Erreur lors de la mise à jour.';
            this.toastr.error(msg);
          }
        });
    } else {
      if (!this.typeId) {
        this.toastr.error('Type d’environnement introuvable.');
        return;
      }
      this.environnementService
        .createEnvironnement({
          code: this.form.code,
          libelle: this.form.libelle,
          description: this.form.description,
          projetId: this.projectId,
          typeId: this.typeId
        })
        .subscribe({
          next: () => {
            this.toastr.success('Environnement créé.');
            this.showForm = false;
            this.loadEnvironments();
          },
          error: (err) => {
            const msg = err.error?.message || 'Erreur lors de la création.';
            this.toastr.error(msg);
          }
        });
    }
  }

  deleteEnv(env: Environnement): void {
    if (!confirm(`Supprimer l'environnement ${env.code} ?`)) {
      return;
    }

    this.environnementService.deleteEnvironnement(env.id).subscribe({
      next: () => {
        this.toastr.success('Environnement supprimé.');
        this.loadEnvironments();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression.';
        this.toastr.error(msg);
      }
    });
  }

  openApplications(env: Environnement): void {
    this.router.navigate([
      '/environment',
      this.typeCode,
      'projects',
      this.projectId,
      'environments',
      env.id,
      'applications'
    ]);
  }
}
