// src/app/permissions/components/permission-management/permission-management.component.ts

import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  ActionType,
  ProfilPermissions,
  ProfilSimple,
  SavePermissionsRequest
} from '../models/permission.model';
import {PermissionService} from '../services/permission.service';

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

  // Toutes les actions disponibles
  allActions: ActionType[] = ['CONSULT', 'CREATE', 'UPDATE', 'DELETE'];

  // État des checkboxes (Map pour performance)
  envTypeActionsMap = new Map<string, Set<ActionType>>();
  projectActionsSet = new Set<ActionType>();
  environmentActionsSet = new Set<ActionType>();

  constructor(
    private permissionService: PermissionService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadProfils();
  }

  loadProfils(): void {
    this.loading = true;
    this.permissionService.getProfils().subscribe({
      next: (profils) => {
        this.profils = profils.filter(p => p.id !== null);
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement des profils', 'Fermer', { duration: 3000 });
        this.loading = false;
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

    // Réinitialiser
    this.envTypeActionsMap.clear();
    this.projectActionsSet.clear();
    this.environmentActionsSet.clear();

    // Types d'environnement
    this.permissions.envTypePermissions.forEach(envType => {
      this.envTypeActionsMap.set(envType.typeCode, new Set(envType.actions));
    });

    // Projets
    this.permissions.projectActions.forEach(action => {
      this.projectActionsSet.add(action);
    });

    // Environnements
    this.permissions.environmentActions.forEach(action => {
      this.environmentActionsSet.add(action);
    });
  }

  // ========== Gestion des checkboxes ENV_TYPE ==========

  isEnvTypeActionChecked(typeCode: string, action: ActionType): boolean {
    return this.envTypeActionsMap.get(typeCode)?.has(action) ?? false;
  }

  toggleEnvTypeAction(typeCode: string, action: ActionType): void {
    if (!this.envTypeActionsMap.has(typeCode)) {
      this.envTypeActionsMap.set(typeCode, new Set());
    }

    const actions = this.envTypeActionsMap.get(typeCode)!;

    if (actions.has(action)) {
      actions.delete(action);
    } else {
      actions.add(action);
    }
  }

  // ========== Gestion des checkboxes PROJECT ==========

  isProjectActionChecked(action: ActionType): boolean {
    return this.projectActionsSet.has(action);
  }

  toggleProjectAction(action: ActionType): void {
    if (this.projectActionsSet.has(action)) {
      this.projectActionsSet.delete(action);
    } else {
      this.projectActionsSet.add(action);
    }
  }

  // ========== Gestion des checkboxes ENVIRONMENT ==========

  isEnvironmentActionChecked(action: ActionType): boolean {
    return this.environmentActionsSet.has(action);
  }

  toggleEnvironmentAction(action: ActionType): void {
    if (this.environmentActionsSet.has(action)) {
      this.environmentActionsSet.delete(action);
    } else {
      this.environmentActionsSet.add(action);
    }
  }

  // ========== Sauvegarde ==========

  save(): void {
    if (!this.selectedProfilId || !this.permissions) {
      return;
    }

    this.saving = true;

    // Construire le payload
    const envTypePermissions: { [key: string]: ActionType[] } = {};

    this.permissions.envTypePermissions.forEach(envType => {
      const actions = this.envTypeActionsMap.get(envType.typeCode);
      envTypePermissions[envType.typeCode] = actions ? Array.from(actions) : [];
    });

    const request: SavePermissionsRequest = {
      profilId: this.selectedProfilId,
      envTypePermissions,
      projectActions: Array.from(this.projectActionsSet),
      environmentActions: Array.from(this.environmentActionsSet)
    };

    this.permissionService.savePermissions(this.selectedProfilId, request).subscribe({
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
