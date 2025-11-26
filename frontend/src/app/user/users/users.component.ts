import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { TableConfig } from '../../../configuration/data-table.config';
import { UserService } from '../../environement/services/user-service';
import { UserFormComponent } from '../user-form/user-form.component';

@Component({
  selector: 'app-users',
  standalone: false,
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

  users: any[] = [];

  tableConfig: TableConfig = {
    columns: [
      { field: 'id', header: 'ID' },
      { field: 'code', header: 'Code' },
      { field: 'firstName', header: 'Prénom' },
      { field: 'lastName', header: 'Nom' },
      { field: 'email', header: 'Email' },
      { field: 'profilLibelle', header: 'Profil' },
      { field: 'actif', header: 'Actif', type: 'boolean' }
    ],
    enableSearch: true,
    enableSort: true,
    enablePagination: true
  };

  constructor(
    private userService: UserService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.list().subscribe(res => this.users = res);
  }

  onAdd() {
    const dialogRef = this.dialog.open(UserFormComponent, { width: '450px' });

    dialogRef.afterClosed().subscribe(saved => {
      if (saved) this.loadUsers();
    });
  }

  onEdit(user: any) {
    const dialogRef = this.dialog.open(UserFormComponent, {
      width: '450px',
      data: user
    });

    dialogRef.afterClosed().subscribe(updated => {
      if (updated) this.loadUsers();
    });
  }

  onDelete(user: any) {
    if (!confirm('Voulez-vous supprimer cet utilisateur ?')) return;

    this.userService.delete(user.id).subscribe(() => this.loadUsers());
  }
}
