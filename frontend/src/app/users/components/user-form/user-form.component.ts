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
  hidePassword = true;

  profils: ProfilDTO[] = [];
  loading = false;
  saving = false;
  profilsLoading = false;

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

    if (!this.isEdit) {
      this.form.get('password')?.setValidators([
        Validators.required,
        Validators.minLength(8)
      ]);
      this.form.get('password')?.updateValueAndValidity();
    }

    if (this.isEdit && this.data.userId) {
      this.userId = this.data.userId;
      this.loadUser(this.userId);
    } else {
      this.initialFormValue = this.form.value;
      this.loading = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  buildForm(): void {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      actif: [true],
      profilId: [null, Validators.required],
      password: ['']
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
        error: () => {
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
          if (this.isEdit) {
            this.form.get('code')?.disable();
          }
          this.initialFormValue = this.form.value;
          this.loading = false;
        },
        error: () => {
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
    const payload = this.form.getRawValue();

    if (this.isEdit && !payload.password) {
      delete payload.password;
    }

    const obs = this.isEdit && this.userId
      ? this.userService.update(this.userId, payload)
      : this.userService.create(payload);

    obs
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.saving = false;
          const message = this.isEdit
            ? 'Utilisateur modifie avec succes'
            : 'Utilisateur cree avec succes';
          this.showSuccess(message);
          this.dialogRef.close(true);
        },
        error: () => {
          this.saving = false;
          const message = this.isEdit
            ? 'Erreur lors de la modification de l\'utilisateur'
            : 'Erreur lors de la creation de l\'utilisateur';
          this.showError(message);
        }
      });
  }
  getSelectedProfilCode(): string {
    const id = this.form.get('profilId')?.value;
    if (!id || !this.profils.length) return '';

    const profil = this.profils.find(p => p.id === id);
    return profil ? profil.code : '';
  }

  cancel(): void {
    if (this.hasFormChanged()) {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        width: '450px',
        data: {
          title: 'Abandonner les modifications',
          message: 'Vous avez des modifications non enregistrees. Êtes-vous sur de vouloir quitter ?',
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

  setActif(value: boolean): void {
    this.form.get('actif')?.setValue(value);
  }

  private hasFormChanged(): boolean {
    if (!this.initialFormValue) return false;
    return JSON.stringify(this.form.value) !== JSON.stringify(this.initialFormValue);
    }

  private showSuccess(message: string): void {
    this.snackBar.open(message, '', {
      duration: 3000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['toast-success'],
      announcementMessage: message
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, '', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['toast-error'],
      announcementMessage: message
    });
  }

  get codeControl() { return this.form.get('code'); }
  get firstNameControl() { return this.form.get('firstName'); }
  get lastNameControl() { return this.form.get('lastName'); }
  get emailControl() { return this.form.get('email'); }
  get profilIdControl() { return this.form.get('profilId'); }
  get actifControl() { return this.form.get('actif'); }
  get passwordControl() { return this.form.get('password'); }

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
      return `Minimum ${minLength} caracteres requis`;
    }
    if (control.hasError('maxlength')) {
      const maxLength = control.errors['maxlength'].requiredLength;
      return `Maximum ${maxLength} caracteres autorises`;
    }

    return 'Valeur invalide';
  }
}
