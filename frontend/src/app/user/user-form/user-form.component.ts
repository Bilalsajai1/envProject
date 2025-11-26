import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';
import { UserService } from '../../environement/services/user-service';
import { ProfilService } from '../../environement/services/profil-service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-user-form',
  standalone: false,
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss',
})
export class UserFormComponent implements OnInit {

  profils: any[] = [];
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private profilService: ProfilService,
    private dialogRef: MatDialogRef<UserFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    // ⚠️ initialisation dans le constructeur pour éviter "used before initialization"
    this.form = this.fb.group({
      id: [null],
      code: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      actif: [true],
      profilId: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.profilService.getAll().subscribe(res => this.profils = res);

    if (this.data) {
      this.form.patchValue(this.data);
    }
  }

  save() {
    if (this.form.invalid) return;

    const payload = this.form.value;

    if (!payload.id) {
      this.userService.create(payload).subscribe(() => this.dialogRef.close(true));
    } else {
      this.userService.update(payload.id, payload).subscribe(() => this.dialogRef.close(true));
    }
  }

  close() {
    this.dialogRef.close(false);
  }
}
