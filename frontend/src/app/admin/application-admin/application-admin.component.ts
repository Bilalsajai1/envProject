// src/app/admin/pages/application-admin/application-admin.component.ts
import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import {Application} from '../../environement/models/application.model';
import {ApplicationAdminService} from '../../environement/services/application-admin';

@Component({
  selector: 'app-application-admin',
  standalone: false,
  templateUrl: './application-admin.component.html',
  styleUrls: ['./application-admin.component.scss']
})
export class ApplicationAdminComponent implements OnInit {

  applications: Application[] = [];
  loading = false;
  error?: string;

  showForm = false;
  isEditing = false;
  currentId?: number;

  form: Partial<Application> = {
    code: '',
    libelle: '',
    description: '',
    actif: true
  };

  constructor(
    private appService: ApplicationAdminService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.appService.getAllApplications().subscribe({
      next: (data) => {
        this.applications = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des applications.';
        this.toastr.error(this.error);
        this.loading = false;
      }
    });
  }

  openCreateForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.currentId = undefined;
    this.form = {
      code: '',
      libelle: '',
      description: '',
      actif: true
    };
  }

  openEditForm(app: Application): void {
    this.showForm = true;
    this.isEditing = true;
    this.currentId = app.id;
    this.form = { ...app };
  }

  cancelForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.currentId = undefined;
  }

  submitForm(): void {
    if (!this.form.code || !this.form.libelle) {
      this.toastr.warning('Code et libellé sont obligatoires.');
      return;
    }

    const payload: Partial<Application> = {
      code: this.form.code,
      libelle: this.form.libelle,
      description: this.form.description,
      actif: this.form.actif ?? true
    };

    if (this.isEditing && this.currentId) {
      this.appService.updateApplication(this.currentId, payload).subscribe({
        next: () => {
          this.toastr.success('Application mise à jour avec succès.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la mise à jour de l’application.';
          this.toastr.error(msg);
        }
      });
    } else {
      this.appService.createApplication(payload).subscribe({
        next: () => {
          this.toastr.success('Application créée avec succès.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la création de l’application.';
          this.toastr.error(msg);
        }
      });
    }
  }

  delete(app: Application): void {
    if (!confirm(`Supprimer l'application ${app.code} ?`)) {
      return;
    }

    this.appService.deleteApplication(app.id).subscribe({
      next: () => {
        this.toastr.success('Application supprimée avec succès.');
        this.load();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression de l’application.';
        this.toastr.error(msg);
      }
    });
  }
}
