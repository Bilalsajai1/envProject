import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ProfilService } from '../../environement/services/profil-service';
import { RoleService } from '../../environement/services/role-service';
import { Profil } from '../../environement/models/profil.model';
import { Role } from '../../environement/models/role.model';

@Component({
  selector: 'app-role-admin',
  standalone: false,
  templateUrl: './role-admin.component.html',
  styleUrls: ['./role-admin.component.scss']
})
export class RoleAdminComponent implements OnInit {

  profils: Profil[] = [];
  roles: Role[] = [];

  loadingProfils = false;
  loadingRoles = false;
  saving = false;

  profilSearch = '';

  selectedProfil?: Profil;
  /** Set des IDs de rôles assignés au profil sélectionné */
  selectedRoleIds = new Set<number>();

  constructor(
    private profilService: ProfilService,
    private roleService: RoleService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadProfils();
    this.loadRoles();
  }

  // ---------------- PROFILS ----------------

  loadProfils(): void {
    this.loadingProfils = true;
    this.profilService.getAll().subscribe({
      next: (profils: Profil[]) => {
        this.profils = profils;
        this.loadingProfils = false;

        // auto-sélection du premier profil si rien n'est sélectionné
        if (!this.selectedProfil && this.profils.length > 0) {
          this.selectProfil(this.profils[0]);
        }
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des profils.');
        this.loadingProfils = false;
      }
    });
  }

  filteredProfils(): Profil[] {
    const q = this.profilSearch.trim().toLowerCase();
    if (!q) return this.profils;
    return this.profils.filter(p =>
      (p.libelle && p.libelle.toLowerCase().includes(q)) ||
      (p.code && p.code.toLowerCase().includes(q))
    );
  }

  selectProfil(p: Profil): void {
    this.selectedProfil = p;
    this.selectedRoleIds.clear();

    this.profilService.getRolesByProfil(p.id).subscribe({
      next: (ids: number[]) => {
        ids.forEach(id => this.selectedRoleIds.add(id));
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des rôles du profil.');
      }
    });
  }

  getProfilInitials(p: Profil): string {
    if (p.libelle) {
      const parts = p.libelle.split(' ');
      const first = parts[0]?.charAt(0) ?? '';
      const second = parts[1]?.charAt(0) ?? '';
      return (first + second).toUpperCase();
    }
    return (p.code || '?').substring(0, 2).toUpperCase();
  }

  // ---------------- RÔLES ----------------

  loadRoles(): void {
    this.loadingRoles = true;
    this.roleService.getAllRoles().subscribe({
      next: (roles: Role[]) => {
        this.roles = roles;
        this.loadingRoles = false;
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des rôles.');
        this.loadingRoles = false;
      }
    });
  }

  isRoleSelected(id: number): boolean {
    return this.selectedRoleIds.has(id);
  }

  toggleRole(id: number, checked: boolean): void {
    if (checked) {
      this.selectedRoleIds.add(id);
    } else {
      this.selectedRoleIds.delete(id);
    }
  }

  // ---------------- SAVE ----------------

  save(): void {
    if (!this.selectedProfil) {
      this.toastr.warning('Sélectionne un profil d’abord.');
      return;
    }

    const roleIds = Array.from(this.selectedRoleIds);
    this.saving = true;

    this.profilService.updateProfilRoles(this.selectedProfil.id, roleIds).subscribe({
      next: () => {
        this.toastr.success('Permissions mises à jour avec succès.');
        this.saving = false;
      },
      error: (err: any) => {
        console.error(err);
        const msg = err.error?.message || 'Erreur lors de la mise à jour des permissions.';
        this.toastr.error(msg);
        this.saving = false;
      }
    });
  }
}
