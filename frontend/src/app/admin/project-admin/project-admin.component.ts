import {Component, OnInit} from '@angular/core';
import {Projet} from '../../environement/models/projet.model';
import {ProjetService} from '../../environement/services/projet-service';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-project-admin',
  standalone: false,
  templateUrl: './project-admin.component.html',
  styleUrl: './project-admin.component.scss',
})
export class ProjectAdminComponent implements OnInit {

  projects: Projet[] = [];
  loading = false;
  error?: string;

  showForm = false;
  isEditing = false;
  currentId?: number;

  form: Partial<Projet> = {
    code: '',
    libelle: '',
    description: '',
    actif: true
  };

  constructor(
    private projetService: ProjetService,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.projetService.getAllProjects().subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des projets.';
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

  openEditForm(p: Projet): void {
    this.showForm = true;
    this.isEditing = true;
    this.currentId = p.id;
    this.form = { ...p };
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

    const payload: Partial<Projet> = {
      code: this.form.code,
      libelle: this.form.libelle,
      description: this.form.description,
      actif: this.form.actif ?? true
    };

    if (this.isEditing && this.currentId) {
      this.projetService.updateProject(this.currentId, payload).subscribe({
        next: () => {
          this.toastr.success('Projet mis à jour avec succès.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la mise à jour du projet.';
          this.toastr.error(msg);
        }
      });
    } else {
      this.projetService.createProject(payload).subscribe({
        next: () => {
          this.toastr.success('Projet créé avec succès.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la création du projet.';
          this.toastr.error(msg);
        }
      });
    }
  }

  delete(p: Projet): void {
    if (!confirm(`Supprimer le projet ${p.code} ?`)) {
      return;
    }

    this.projetService.deleteProject(p.id).subscribe({
      next: () => {
        this.toastr.success('Projet supprimé avec succès.');
        this.load();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression du projet.';
        this.toastr.error(msg);
      }
    });
  }
}
