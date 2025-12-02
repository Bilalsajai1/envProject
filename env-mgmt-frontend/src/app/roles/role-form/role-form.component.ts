// src/app/roles/role-form/role-form.component.ts

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import {
  RoleService,
  RoleDTO,
  RoleCreateUpdateDTO,
  MenuDTO,
  EnvironnementDTO,
  ProjetDTO
} from '../services/role.service';
import { ConfirmDialogComponent } from '../../users/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-role-form',
  templateUrl: './role-form.component.html',
  styleUrls: ['./role-form.component.scss'],
  standalone: false
})
export class RoleFormComponent implements OnInit, OnDestroy {

  form!: FormGroup;
  isEdit = false;
  roleId?: number;

  menus: MenuDTO[] = [];
  environnements: EnvironnementDTO[] = [];
  projets: ProjetDTO[] = [];

  loading = false;
  saving = false;
  loadingOptions = false;

  // Liste des actions disponibles
  actions: string[] = [
    'CONSULT',
    'CREATE',
    'UPDATE',
    'DELETE',
    'EXPORT',
    'IMPORT',
    'VALIDATE',
    'APPROVE',
    'REJECT'
  ];

  // Pour détecter si le formulaire a été modifié
  initialFormValue: any;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private roleService: RoleService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.loadOptions();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = true;
      this.roleId = Number(idParam);
      this.loadRole(this.roleId);
    } else {
      this.initialFormValue = this.form.value;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  buildForm(): void {
    this.form = this.fb.group({
      code: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(100),
        Validators.pattern(/^[A-Z0-9_]+$/)
      ]],
      libelle: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(200)
      ]],
      action: ['', Validators.required],
      menuId: [null],
      environnementId: [null],
      projetId: [null],
      actif: [true]
    });
  }

  loadOptions(): void {
    this.loadingOptions = true;

    forkJoin({
      menus: this.roleService.getMenus(),
      environnements: this.roleService.getEnvironnements(),
      projets: this.roleService.getProjets()
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          this.menus = result.menus;
          this.environnements = result.environnements;
          this.projets = result.projets;
          this.loadingOptions = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des options:', error);
          this.showError('Impossible de charger les options');
          this.loadingOptions = false;
        }
      });
  }

  loadRole(id: number): void {
    this.loading = true;
    this.roleService.getById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (role: RoleDTO) => {
          this.form.patchValue({
            code: role.code,
            libelle: role.libelle,
            action: role.action,
            menuId: role.menuId,
            environnementId: role.environnementId,
            projetId: role.projetId,
            actif: role.actif
          });

          // Sauvegarder la valeur initiale après le chargement
          this.initialFormValue = this.form.value;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement du rôle:', error);
          this.showError('Impossible de charger le rôle');
          this.loading = false;
          this.router.navigate(['/admin/roles']);
        }
      });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.showError('Veuillez corriger les erreurs dans le formulaire');
      return;
    }

    this.saving = true;
    const payload: RoleCreateUpdateDTO = this.form.value;

    const obs = this.isEdit && this.roleId
      ? this.roleService.update(this.roleId, payload)
      : this.roleService.create(payload);

    obs.pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving = false;
          const message = this.isEdit
            ? 'Rôle modifié avec succès'
            : 'Rôle créé avec succès';
          this.showSuccess(message);

          setTimeout(() => {
            this.router.navigate(['/admin/roles']);
          }, 500);
        },
        error: (error) => {
          console.error('Erreur lors de l\'enregistrement:', error);
          this.saving = false;
          const message = this.isEdit
            ? 'Erreur lors de la modification du rôle'
            : 'Erreur lors de la création du rôle';
          this.showError(message);
        }
      });
  }

  cancel(): void {
    if (this.hasFormChanged()) {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        width: '450px',
        data: {
          title: 'Abandonner les modifications',
          message: 'Vous avez des modifications non enregistrées. Êtes-vous sûr de vouloir quitter ?',
          confirmText: 'Quitter',
          cancelText: 'Rester',
          type: 'warning'
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.router.navigate(['/admin/roles']);
        }
      });
    } else {
      this.router.navigate(['/admin/roles']);
    }
  }

  private hasFormChanged(): boolean {
    if (!this.initialFormValue) return false;
    return JSON.stringify(this.form.value) !== JSON.stringify(this.initialFormValue);
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }

  // Getters pour faciliter l'accès aux contrôles
  get codeControl() {
    return this.form.get('code');
  }

  get libelleControl() {
    return this.form.get('libelle');
  }

  get actionControl() {
    return this.form.get('action');
  }

  get menuIdControl() {
    return this.form.get('menuId');
  }

  get environnementIdControl() {
    return this.form.get('environnementId');
  }

  get projetIdControl() {
    return this.form.get('projetId');
  }

  get actifControl() {
    return this.form.get('actif');
  }

  // Helper pour obtenir les messages d'erreur
  getErrorMessage(controlName: string): string {
    const control = this.form.get(controlName);
    if (!control || !control.errors) return '';

    if (control.hasError('required')) {
      return 'Ce champ est obligatoire';
    }
    if (control.hasError('minlength')) {
      const minLength = control.errors['minlength'].requiredLength;
      return `Minimum ${minLength} caractères requis`;
    }
    if (control.hasError('maxlength')) {
      const maxLength = control.errors['maxlength'].requiredLength;
      return `Maximum ${maxLength} caractères autorisés`;
    }
    if (control.hasError('pattern')) {
      return 'Format invalide (lettres majuscules, chiffres et underscore uniquement)';
    }

    return 'Valeur invalide';
  }
}
