import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProfilService } from '../../environement/services/profil-service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-profil-role',
  standalone: false,
  templateUrl: './profil-role.component.html',
  styleUrls: ['./profil-role.component.scss']
})
export class ProfilRoleComponent implements OnInit {

  profilId!: number;
  profil?: any;

  allRoles: any[] = [];
  assignedRoles: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private profilService: ProfilService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.profilId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadData();
  }

  loadData() {
    this.profilService.getById(this.profilId).subscribe(p => this.profil = p);

    this.profilService.getAllRoles().subscribe(all => this.allRoles = all);

    this.profilService.getRolesByProfil(this.profilId).subscribe(roles => {
      this.assignedRoles = roles;
    });
  }

  isAssigned(role: any): boolean {
    return !!this.assignedRoles.find(r => r.id === role.id);
  }

  add(role: any) {
    if (!this.isAssigned(role)) {
      this.assignedRoles.push(role);
    }
  }

  remove(role: any) {
    this.assignedRoles = this.assignedRoles.filter(r => r.id !== role.id);
  }

  saveRoles() {
    const roleIds = this.assignedRoles.map(r => r.id);
    this.profilService.updateProfilRoles(this.profilId, roleIds).subscribe({
      next: () => this.toastr.success("Rôles mis à jour"),
      error: () => this.toastr.error("Erreur mise à jour")
    });
  }
}
