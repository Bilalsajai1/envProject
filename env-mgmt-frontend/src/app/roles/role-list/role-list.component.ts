import { Component, OnInit } from '@angular/core';

interface RoleRow {
  id: number;
  code: string;
  libelle: string;
  description?: string;
  actif: boolean;
}

@Component({
  selector: 'app-role-list',
  templateUrl: './role-list.component.html',
  styleUrls: ['./role-list.component.scss'],
  standalone: false
})
export class RoleListComponent implements OnInit {

  loading = false;
  roles: RoleRow[] = [];

  constructor() {}

  ngOnInit(): void {
    // TODO: appeler RoleService plus tard
    this.roles = [
      { id: 1, code: 'ENV_EDITION_CONSULT', libelle: 'Consultation Edition', description: '', actif: true },
      { id: 2, code: 'ENV_EDITION_UPDATE', libelle: 'Mise à jour Edition', description: '', actif: true },
      { id: 3, code: 'ENV_CLIENT_CONSULT', libelle: 'Consultation Client', description: '', actif: true }
    ];
  }

  add(): void {
    // TODO: navigation vers /admin/roles/new
  }

  edit(role: RoleRow): void {
    // TODO: navigation vers /admin/roles/{id}/edit
  }
}
