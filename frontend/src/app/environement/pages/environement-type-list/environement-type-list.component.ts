import {Component, OnInit} from '@angular/core';
import {EnvironmentType} from '../../models/environment-type.model';
import {EnvironmentTypeService} from '../../services/environnement-type-service';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-environement-type-list',
  standalone: false,
  templateUrl: './environement-type-list.component.html',
  styleUrl: './environement-type-list.component.scss',
})
export class EnvironmentTypeListComponent implements  OnInit {

  types: EnvironmentType[] = [];
  loading = false;
  error?: string;

  showForm = false;
  isEditing = false;
  currentId?: number;

  form: Partial<EnvironmentType> = {
    code: '',
    libelle: '',
    actif: true
  };

  constructor(
    private service: EnvironmentTypeService,
    private router: Router,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.getAllTypes().subscribe({
      next: (data) => {
        this.types = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur chargement types';
        this.toastr.error(this.error);
        this.loading = false;
      }
    });
  }
  goToTypeProjects(type: EnvironmentType): void {
    if (!type.code) { return; }
    this.router.navigate(['/environment', type.code, 'projects']);
  }

  openCreateForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.currentId = undefined;
    this.form = { code: '', libelle: '', actif: true };
  }

  openEditForm(type: EnvironmentType): void {
    this.showForm = true;
    this.isEditing = true;
    this.currentId = type.id;
    this.form = { ...type };
  }

  cancel(): void {
    this.showForm = false;
    this.isEditing = false;
    this.currentId = undefined;
  }

  submit(): void {
    if (!this.form.code || !this.form.libelle) {
      this.toastr.warning('Code et libellé obligatoires.');
      return;
    }

    if (this.isEditing && this.currentId) {
      this.service.update(this.currentId, this.form).subscribe({
        next: () => {
          this.toastr.success('Type d’environnement mis à jour.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la mise à jour.';
          this.toastr.error(msg);
        }
      });
    } else {
      this.service.create(this.form).subscribe({
        next: () => {
          this.toastr.success('Type d’environnement créé.');
          this.showForm = false;
          this.load();
        },
        error: (err) => {
          const msg = err.error?.message || 'Erreur lors de la création.';
          this.toastr.error(msg);
        }
      });
    }
  }

  delete(type: EnvironmentType): void {
    if (!confirm('Supprimer ce type ?')) return;

    this.service.delete(type.id).subscribe({
      next: () => {
        this.toastr.success('Type d’environnement supprimé.');
        this.load();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la suppression.';
        this.toastr.error(msg);
      }
    });
  }
}
