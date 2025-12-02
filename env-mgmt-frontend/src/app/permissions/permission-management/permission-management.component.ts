// src/app/permissions/components/permission-management/permission-management.component.ts

import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  ActionType, EnvTypePermissionUpdate,
  ProfilPermissions,
  ProfilSimple,
  ProjectPermissionUpdate,
  SavePermissionsRequest
} from '../models/permission.model';
import {PermissionService} from '../services/permission.service';
import {ActivatedRoute} from '@angular/router';


@Component({
  selector: 'app-permission-management',
  standalone: false,
  templateUrl: './permission-management.component.html',
  styleUrls: ['./permission-management.component.scss']
})
export class PermissionManagementComponent implements OnInit {

  profils: ProfilSimple[] = [];
  selectedProfilId?: number;
  permissions?: ProfilPermissions;

  loading = false;
  saving = false;

  allActions: ActionType[] = ['CONSULT', 'CREATE', 'UPDATE', 'DELETE'];

  envTypeActionsMap = new Map<string, Set<ActionType>>();
  projectActionsMap = new Map<number, Set<ActionType>>();

  constructor(
    private permissionService: PermissionService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfils();
    this.route.queryParams.subscribe(params => {
      if (params['profilId']) {
        const profilId = Number(params['profilId']);
        this.selectedProfilId = profilId;
        this.onProfilChange(profilId);
      }
    });
  }

  loadProfils(): void {
    this.loading = true;
    this.permissionService.getProfils().subscribe({
      next: (profils) => {
        this.profils = profils.filter(p => p.id !== null);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement des profils', 'Fermer', { duration: 3000 });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onProfilChange(profilId: number): void {
    if (!profilId) {
      this.permissions = undefined;
      return;
    }

    this.selectedProfilId = profilId;
    this.loading = true;

    this.permissionService.getPermissions(profilId).subscribe({
      next: (permissions) => {
        this.permissions = permissions;
        this.initializeCheckboxStates();
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement des permissions', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  private initializeCheckboxStates(): void {
    if (!this.permissions) return;

    // Reset
    this.envTypeActionsMap.clear();
    this.projectActionsMap.clear();

    // ✅ Types d'environnement
    if (!this.permissions.envTypePermissions || !Array.isArray(this.permissions.envTypePermissions)) {
      console.warn('⛔ Pas de envTypePermissions dans la réponse backend :', this.permissions);
    } else {
      this.permissions.envTypePermissions.forEach(envType => {
        this.envTypeActionsMap.set(envType.typeCode, new Set(envType.actions));
      });
    }

    // ✅ Projets (nouveau)
    if (this.permissions.projectPermissions && Array.isArray(this.permissions.projectPermissions)) {
      this.permissions.projectPermissions.forEach(proj => {
        this.projectActionsMap.set(
          proj.projectId,
          new Set(proj.actions ?? [])
        );
      });
    }

  }


  isEnvTypeActionChecked(typeCode: string, action: ActionType): boolean {
    return this.envTypeActionsMap.get(typeCode)?.has(action) ?? false;
  }

  toggleEnvTypeAction(typeCode: string, action: ActionType): void {
    if (!this.envTypeActionsMap.has(typeCode)) {
      this.envTypeActionsMap.set(typeCode, new Set());
    }
    const actions = this.envTypeActionsMap.get(typeCode)!;
    actions.has(action) ? actions.delete(action) : actions.add(action);
  }

  isProjectActionChecked(projectId: number, action: ActionType): boolean {
    return this.projectActionsMap.get(projectId)?.has(action) ?? false;
  }
  toggleProjectAction(projectId: number, action: ActionType): void {
    if (!this.projectActionsMap.has(projectId)) {
      this.projectActionsMap.set(projectId, new Set<ActionType>());
    }

    const actions = this.projectActionsMap.get(projectId)!;

    if (actions.has(action)) {
      actions.delete(action);
    } else {
      actions.add(action);
    }
  }





  save(): void {
    if (!this.selectedProfilId || !this.permissions) {
      return;
    }

    this.saving = true;

    // 1️⃣ Env types → EnvTypePermissionUpdate[]
    const envUpdates: EnvTypePermissionUpdate[] = [];
    this.permissions.envTypePermissions.forEach(envType => {
      const actionsSet = this.envTypeActionsMap.get(envType.typeCode) ?? new Set<ActionType>();
      envUpdates.push({
        envTypeCode: envType.typeCode,
        actions: Array.from(actionsSet)
      });
    });

    // 2️⃣ Projets → ProjectPermissionUpdate[]
    const projUpdates: ProjectPermissionUpdate[] = [];
    if (this.permissions.projectPermissions) {
      this.permissions.projectPermissions.forEach(proj => {
        const actionsSet = this.projectActionsMap.get(proj.projectId) ?? new Set<ActionType>();
        projUpdates.push({
          projectId: proj.projectId,
          actions: Array.from(actionsSet)
        });
      });
    }

    // 3️⃣ Envoi vers backend (2 endpoints en parallèle)
    this.permissionService.savePermissions(this.selectedProfilId, envUpdates, projUpdates).subscribe({
      next: () => {
        this.snackBar.open('✅ Permissions enregistrées avec succès', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.saving = false;
      },
      error: (err) => {
        console.error('Erreur sauvegarde permissions:', err);
        this.snackBar.open('❌ Erreur lors de la sauvegarde', 'Fermer', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        this.saving = false;
      }
    });
  }


  cancel(): void {
    if (this.selectedProfilId) {
      this.onProfilChange(this.selectedProfilId);
    }
  }
}
