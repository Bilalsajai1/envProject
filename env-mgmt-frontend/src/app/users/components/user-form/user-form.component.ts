// src/app/admin/users/user-form/user-form.component.ts

import {
  Component,
  OnInit,
  OnDestroy,
  Inject
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA
} from '@angular/material/dialog';

import { UserService } from '../../services/user.service';
import { ProfilDTO, UserDTO } from '../../models/user.model';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog.component';

export interface UserFormData {
  mode: 'create' | 'edit';
  userId?: number;
}

@Component({
  selector: 'app-user-form',
  standalone: false,
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit, OnDestroy {

  form!: FormGroup;
  isEdit = false;
  userId?: number;

  profils: ProfilDTO[] = [];
  loading = false;
  saving = false;
  profilsLoading = false;

  // Pour détecter si le formulaire a été modifié
  initialFormValue: any;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private dialogRef: MatDialogRef<UserFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserFormData
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.loadProfils();

    this.isEdit = this.data.mode === 'edit' && !!this.data.userId;
    if (this.isEdit && this.data.userId) {
      this.userId = this.data.userId;
      this.loadUser(this.userId);
    } else {
      // valeur initiale en mode création
      this.initialFormValue = this.form.value;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  buildForm(): void {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(200)]],
      actif: [true],
      profilId: [null, Validators.required]
    });
  }

  loadProfils(): void {
    this.profilsLoading = true;
    this.userService.getProfils()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: profils => {
          this.profils = profils;
          this.profilsLoading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement des profils:', error);
          this.showError('Impossible de charger les profils');
          this.profilsLoading = false;
        }
      });
  }

  loadUser(id: number): void {
    this.loading = true;
    this.userService.getById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user: UserDTO) => {
          this.form.patchValue({
            code: user.code,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            actif: user.actif,
            profilId: user.profilId
          });

          // Sauvegarder la valeur initiale après le chargement
          this.initialFormValue = this.form.value;
          this.loading = false;
        },
        error: (error) => {
          console.error('Erreur lors du chargement de l\'utilisateur:', error);
          this.showError('Impossible de charger l\'utilisateur');
          this.loading = false;
          this.dialogRef.close(false);
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
    const payload = this.form.value;

    const obs = this.isEdit && this.userId
      ? this.userService.update(this.userId, payload)
      : this.userService.create(payload);

    obs
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving = false;
          const message = this.isEdit
            ? 'Utilisateur modifié avec succès'
            : 'Utilisateur créé avec succès';
          this.showSuccess(message);

          // ✅ on ferme le dialog et on indique au parent de recharger la liste
          this.dialogRef.close(true);
        },
        error: (error) => {
          console.error('Erreur lors de l\'enregistrement:', error);
          this.saving = false;
          const message = this.isEdit
            ? 'Erreur lors de la modification de l\'utilisateur'
            : 'Erreur lors de la création de l\'utilisateur';
          this.showError(message);
        }
      });
  }

  cancel(): void {
    // Vérifier si le formulaire a été modifié
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
          this.dialogRef.close(false);
        }
      });
    } else {
      this.dialogRef.close(false);
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

  // Getters pour faciliter l'accès aux contrôles dans le template
  get codeControl() { return this.form.get('code'); }
  get firstNameControl() { return this.form.get('firstName'); }
  get lastNameControl() { return this.form.get('lastName'); }
  get emailControl() { return this.form.get('email'); }
  get profilIdControl() { return this.form.get('profilId'); }
  get actifControl() { return this.form.get('actif'); }

  // Helper pour obtenir les messages d'erreur
  getErrorMessage(controlName: string): string {
    const control = this.form.get(controlName);
    if (!control || !control.errors) return '';

    if (control.hasError('required')) {
      return 'Ce champ est obligatoire';
    }
    if (control.hasError('email')) {
      return 'Email invalide';
    }
    if (control.hasError('minlength')) {
      const minLength = control.errors['minlength'].requiredLength;
      return `Minimum ${minLength} caractères requis`;
    }
    if (control.hasError('maxlength')) {
      const maxLength = control.errors['maxlength'].requiredLength;
      return `Maximum ${maxLength} caractères autorisés`;
    }

    return 'Valeur invalide';
  }
}
