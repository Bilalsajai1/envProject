// src/app/permissions/components/permission-management/permission-management.component.ts

import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import {
  ActionType,
  EnvTypePermissionUpdate,
  ProfilPermissions,
  ProfilSimple,
  ProjectPermission,
  ProjectPermissionUpdate
} from '../models/permission.model';
import { PermissionService } from '../services/permission.service';

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

  currentStep = 0;
  allActions: ActionType[] = ['CONSULT', 'CREATE', 'UPDATE', 'DELETE'];

  // Étape 1 : Map<envTypeCode, Set<ActionType>>
  envTypeActionsMap = new Map<string, Set<ActionType>>();
  // Étape 2 : Map<projectId, Set<ActionType>>
  projectActionsMap = new Map<number, Set<ActionType>>();

  get selectedProfil(): ProfilSimple | undefined {
    if (!this.selectedProfilId) return undefined;
    return this.profils.find(p => p.id === this.selectedProfilId);
  }

  constructor(
    private permissionService: PermissionService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  get isAdminProfile(): boolean {
    return this.permissions?.isAdmin ?? false;
  }

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
      next: profils => {
        this.profils = profils.filter(p => p.id !== null);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: err => {
        console.error('Erreur chargement profils:', err);
        this.snackBar.open('Erreur lors du chargement des profils', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onProfilChange(profilId: number): void {
    if (!profilId) {
      this.permissions = undefined;
      this.currentStep = 0;
      return;
    }

    this.selectedProfilId = profilId;
    this.loading = true;
    this.currentStep = 0;

    this.permissionService.getPermissions(profilId).subscribe({
      next: permissions => {
        this.permissions = permissions;
        this.initializeCheckboxStates();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: err => {
        console.error('Erreur chargement permissions:', err);
        this.snackBar.open('Erreur lors du chargement des permissions', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  private initializeCheckboxStates(): void {
    if (!this.permissions) return;

    this.envTypeActionsMap.clear();
    this.projectActionsMap.clear();

    // Types d'environnement: charger les actions autorisées
    if (this.permissions.envTypePermissions) {
      this.permissions.envTypePermissions.forEach(envType => {
        this.envTypeActionsMap.set(envType.typeCode, new Set(envType.allowedActions ?? []));
      });
    }

    // Projets: initialiser les actions
    if (this.permissions.projectPermissions) {
      this.permissions.projectPermissions.forEach(proj => {
        this.projectActionsMap.set(proj.projectId, new Set(proj.actions ?? []));
      });
    }
  }

  // Étape 1 : actions par type d'environnement
  isEnvActionChecked(typeCode: string, action: ActionType): boolean {
    return this.envTypeActionsMap.get(typeCode)?.has(action) ?? false;
  }

  toggleEnvAction(typeCode: string, action: ActionType): void {
    if (this.isAdminProfile) {
      this.snackBar.open('Les administrateurs ont automatiquement tous les droits', 'Fermer', { duration: 3000 });
      return;
    }
    if (!this.envTypeActionsMap.has(typeCode)) {
      this.envTypeActionsMap.set(typeCode, new Set<ActionType>());
    }
    const set = this.envTypeActionsMap.get(typeCode)!;
    if (set.has(action)) {
      set.delete(action);
    } else {
      set.add(action);
    }
  }

  // Étape 2 : filtrer les projets selon CONSULT sur les types
  get filteredProjects(): ProjectPermission[] {
    if (!this.permissions) return [];

    // Un projet est affiché si au moins une action a été cochée sur son type
    const selectedTypes = new Set(
      Array.from(this.envTypeActionsMap.entries())
        .filter(([_, actions]) => actions.size > 0)
        .map(([typeCode]) => typeCode.toUpperCase())
    );

    if (selectedTypes.size === 0) return [];

    return this.permissions.projectPermissions.filter(proj => {
      const projectTypes = proj.environmentTypeCodes.map(t => t.toUpperCase());
      return projectTypes.some(t => selectedTypes.has(t));
    });
  }

  isProjectActionChecked(projectId: number, action: ActionType): boolean {
    return this.projectActionsMap.get(projectId)?.has(action) ?? false;
  }

  get selectedEnvTypesCount(): number {
    return Array.from(this.envTypeActionsMap.values()).filter(set => set.size > 0).length;
  }

  toggleProjectAction(projectId: number, action: ActionType): void {
    if (this.isAdminProfile) {
      this.snackBar.open('Les administrateurs ont automatiquement tous les droits', 'Fermer', { duration: 3000 });
      return;
    }
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

  nextStep(): void {
    if (!this.canProceedToStep2() || this.isAdminProfile) return;
    this.currentStep = 1;
    this.cdr.markForCheck();
  }

  previousStep(): void {
    this.currentStep = 0;
    this.cdr.markForCheck();
  }

  save(): void {
    if (this.isAdminProfile) {
      this.snackBar.open('Impossible de modifier un profil administrateur', 'Fermer', { duration: 3000 });
      return;
    }
    if (!this.selectedProfilId || !this.permissions) {
      return;
    }
    this.saving = true;

    const envUpdates: EnvTypePermissionUpdate[] = [];
    this.envTypeActionsMap.forEach((actionsSet, typeCode) => {
      const actions = Array.from(actionsSet);
      if (actions.length > 0) {
        envUpdates.push({ envTypeCode: typeCode, actions });
      }
    });

    const projUpdates: ProjectPermissionUpdate[] = [];
    this.projectActionsMap.forEach((actionsSet, projectId) => {
      const actions = Array.from(actionsSet);
      if (actions.length > 0) {
        projUpdates.push({ projectId, actions });
      }
    });

    this.permissionService.savePermissions(
      this.selectedProfilId,
      envUpdates,
      projUpdates
    ).subscribe({
      next: () => {
        this.snackBar.open('Permissions enregistrées', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.saving = false;
        this.onProfilChange(this.selectedProfilId!);
      },
      error: (err) => {
        console.error('Erreur sauvegarde permissions:', err);
        this.snackBar.open('Erreur lors de la sauvegarde', 'Fermer', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        this.saving = false;
        this.cdr.markForCheck();
      }
    });
  }

  cancel(): void {
    if (this.selectedProfilId) {
      this.onProfilChange(this.selectedProfilId);
    }
  }

  canProceedToStep2(): boolean {
    // Au moins un type avec au moins une action cochée
    return Array.from(this.envTypeActionsMap.values()).some(set => set.size > 0);
  }
}
