import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { ProfilDTO, UserDTO } from '../../models/user.model';

@Component({
  selector: 'app-user-form',
  standalone: false,
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss']
})
export class UserFormComponent implements OnInit {

  form!: FormGroup;
  isEdit = false;
  userId?: number;

  profils: ProfilDTO[] = [];
  loading = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.loadProfils();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdit = true;
      this.userId = Number(idParam);
      this.loadUser(this.userId);
    }
  }

  buildForm(): void {
    this.form = this.fb.group({
      code: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      actif: [true],
      profilId: [null, Validators.required]
    });
  }

  loadProfils(): void {
    this.userService.getProfils().subscribe({
      next: profils => this.profils = profils
    });
  }

  loadUser(id: number): void {
    this.loading = true;
    this.userService.getById(id).subscribe({
      next: (user: UserDTO) => {
        this.form.patchValue({
          code: user.code,
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email,
          actif: user.actif,
          profilId: user.profilId
        });
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;

    const payload = this.form.value;

    const obs = this.isEdit && this.userId
      ? this.userService.update(this.userId, payload)
      : this.userService.create(payload);

    obs.subscribe({
      next: () => {
        this.saving = false;
        this.router.navigate(['/admin/users']);
      },
      error: () => {
        this.saving = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/admin/users']);
  }
}
