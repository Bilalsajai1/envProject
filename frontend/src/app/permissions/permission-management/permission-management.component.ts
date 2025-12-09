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

  // Stepper
  currentStep = 0;

  allActions: ActionType[] = ['CONSULT', 'CREATE', 'UPDATE', 'DELETE'];

  /**
   * ✅ ÉTAPE 1: Types d'environnement cochés
   * Map<typeCode, isChecked>
   */
  envTypeCheckedMap = new Map<string, boolean>();

  /**
   * ✅ ÉTAPE 2: Actions par projet
   * Map<projectId, Set<ActionType>>
   */
  projectActionsMap = new Map<number, Set<ActionType>>();

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
      next: (profils) => {
        this.profils = profils.filter(p => p.id !== null);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('❌ Erreur chargement profils:', err);
        this.snackBar.open('Erreur lors du chargement des profils', 'Fermer', {
          duration: 3000
        });
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
    this.currentStep = 0; // Reset au step 1

    this.permissionService.getPermissions(profilId).subscribe({
      next: (permissions) => {
        console.log('✅ Permissions reçues:', permissions);
        this.permissions = permissions;
        this.initializeCheckboxStates();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('❌ Erreur chargement permissions:', err);
        this.snackBar.open('Erreur lors du chargement des permissions', 'Fermer', {
          duration: 3000
        });
        this.loading = false;
      }
    });
  }

  /**
   * ✅ CORRECTION: Initialiser TOUS les types (cochés et non-cochés)
   */
  private initializeCheckboxStates(): void {
    if (!this.permissions) return;

    this.envTypeCheckedMap.clear();
    this.projectActionsMap.clear();

    // ✅ Types d'environnement: TOUS les types
    // Coché si allowedActions contient CONSULT
    if (this.permissions.envTypePermissions) {
      this.permissions.envTypePermissions.forEach(envType => {
        const isChecked = envType.allowedActions.includes('CONSULT');
        this.envTypeCheckedMap.set(envType.typeCode, isChecked);
        console.log(`📋 Type ${envType.typeCode}: ${isChecked ? 'coché ✅' : 'non coché ❌'}`);
      });
    }

    // ✅ Projets: initialiser les actions
    if (this.permissions.projectPermissions) {
      this.permissions.projectPermissions.forEach(proj => {
        this.projectActionsMap.set(
          proj.projectId,
          new Set(proj.actions ?? [])
        );
      });
    }

    console.log('📋 États initialisés:', {
      envTypes: Array.from(this.envTypeCheckedMap.entries()),
      projects: Array.from(this.projectActionsMap.entries())
    });
  }

  /**
   * ✅ ÉTAPE 1: Type d'environnement
   */
  isEnvTypeChecked(typeCode: string): boolean {
    return this.envTypeCheckedMap.get(typeCode) ?? false;
  }

  toggleEnvType(typeCode: string): void {
    if (this.isAdminProfile) {
      this.snackBar.open(
        '⚠️ Les administrateurs ont automatiquement tous les droits',
        'Fermer',
        { duration: 3000 }
      );
      return;
    }

    const current = this.envTypeCheckedMap.get(typeCode) ?? false;
    this.envTypeCheckedMap.set(typeCode, !current);
    console.log(`🔄 Type ${typeCode} → ${!current ? 'coché ✅' : 'décoché ❌'}`);
  }

  /**
   * ✅ ÉTAPE 2: Filtrer les projets selon les types cochés
   */
  get filteredProjects(): ProjectPermission[] {
    if (!this.permissions) return [];

    // Récupérer les types cochés
    const checkedTypes = new Set(
      Array.from(this.envTypeCheckedMap.entries())
        .filter(([_, isChecked]) => isChecked)
        .map(([typeCode, _]) => typeCode.toUpperCase())
    );

    console.log('✅ Types cochés:', Array.from(checkedTypes));

    if (checkedTypes.size === 0) {
      console.log('⚠️ Aucun type coché, aucun projet affiché');
      return [];
    }

    // Filtrer les projets qui ont au moins un type d'environnement coché
    const filtered = this.permissions.projectPermissions.filter(proj => {
      const projectTypes = proj.environmentTypeCodes.map(t => t.toUpperCase());
      const hasMatchingType = projectTypes.some(t => checkedTypes.has(t));

      if (hasMatchingType) {
        console.log(`✅ Projet ${proj.projectCode} affiché (types: ${projectTypes.join(', ')})`);
      }

      return hasMatchingType;
    });

    console.log(`📋 ${filtered.length} projet(s) affiché(s) sur ${this.permissions.projectPermissions.length}`);

    return filtered;
  }

  /**
   * ✅ Actions sur les projets
   */
  isProjectActionChecked(projectId: number, action: ActionType): boolean {
    return this.projectActionsMap.get(projectId)?.has(action) ?? false;
  }

  toggleProjectAction(projectId: number, action: ActionType): void {
    if (this.isAdminProfile) {
      this.snackBar.open(
        '⚠️ Les administrateurs ont automatiquement tous les droits',
        'Fermer',
        { duration: 3000 }
      );
      return;
    }

    if (!this.projectActionsMap.has(projectId)) {
      this.projectActionsMap.set(projectId, new Set<ActionType>());
    }

    const actions = this.projectActionsMap.get(projectId)!;

    if (actions.has(action)) {
      actions.delete(action);
      console.log(`🔄 Projet ${projectId}: retiré ${action}`);
    } else {
      actions.add(action);
      console.log(`🔄 Projet ${projectId}: ajouté ${action}`);
    }
  }

  /**
   * ✅ Navigation Stepper
   */
  nextStep(): void {
    if (this.currentStep < 1) {
      this.currentStep++;
      console.log('➡️ Step suivant:', this.currentStep);
      this.cdr.markForCheck();
    }
  }

  previousStep(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
      console.log('⬅️ Step précédent:', this.currentStep);
      this.cdr.markForCheck();
    }
  }

  /**
   * ✅ SAUVEGARDE FINALE
   */
  save(): void {
    if (this.isAdminProfile) {
      this.snackBar.open(
        '⚠️ Impossible de modifier les permissions d\'un profil administrateur',
        'Fermer',
        { duration: 3000 }
      );
      return;
    }

    if (!this.selectedProfilId || !this.permissions) {
      return;
    }

    this.saving = true;

    // 1️⃣ Types d'environnement: seulement ceux cochés
    const envUpdates: EnvTypePermissionUpdate[] = [];
    this.envTypeCheckedMap.forEach((isChecked, typeCode) => {
      if (isChecked) {
        envUpdates.push({ envTypeCode: typeCode });
      }
    });

    // 2️⃣ Projets: seulement ceux avec au moins une action
    const projUpdates: ProjectPermissionUpdate[] = [];
    this.projectActionsMap.forEach((actionsSet, projectId) => {
      const actions = Array.from(actionsSet);
      if (actions.length > 0) {
        projUpdates.push({
          projectId,
          actions
        });
      }
    });

    console.log('📤 Sauvegarde:', { envUpdates, projUpdates });

    this.permissionService.savePermissions(
      this.selectedProfilId,
      envUpdates,
      projUpdates
    ).subscribe({
      next: () => {
        this.snackBar.open('✅ Permissions enregistrées avec succès', 'Fermer', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.saving = false;

        // Recharger les permissions pour afficher l'état sauvegardé
        this.onProfilChange(this.selectedProfilId!);
      },
      error: (err) => {
        console.error('❌ Erreur sauvegarde permissions:', err);
        this.snackBar.open('❌ Erreur lors de la sauvegarde', 'Fermer', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        this.saving = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * ✅ Annulation
   */
  cancel(): void {
    if (this.selectedProfilId) {
      this.onProfilChange(this.selectedProfilId);
    }
  }
  /**
   * ✅ Validation Step 1
   */
  canProceedToStep2(): boolean {
    // Au moins un type doit être coché
    const hasCheckedType = Array.from(this.envTypeCheckedMap.values()).some(checked => checked);

    if (!hasCheckedType) {
      console.log('⚠️ Aucun type coché, impossible de passer à l\'étape 2');
    }

    return hasCheckedType;
  }
}
