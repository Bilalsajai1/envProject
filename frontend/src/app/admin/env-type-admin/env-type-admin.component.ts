import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { ProjetService } from '../../environement/services/projet-service';
import {EnvironmentTypeService} from '../../environement/services/environnement-type-service';
import {EnvironnementService} from '../../environement/services/environement-service';
import {EnvApplicationService} from '../../environement/services/env-application-service';

// Petits modèles locaux
interface Projet {
  id: number;
  code: string;
  libelle?: string;
  description?: string;
  actif?: boolean;
}

interface Environnement {
  id: number;
  code: string;
  libelle?: string;
  description?: string;
  actif?: boolean;
}

interface EnvApplication {
  id: number;
  applicationCode?: string;
  applicationLibelle?: string;
  protocole?: string;
  host?: string;
  port?: number;
  url?: string;
  description?: string;
  actif?: boolean;
}

@Component({
  selector: 'app-env-type-admin',
  standalone: false,
  templateUrl: './env-type-admin.component.html',
  styleUrls: ['./env-type-admin.component.scss']
})
export class EnvTypeAdminComponent implements OnInit {

  typeCode = '';
  typeLabel?: string;

  projets: Projet[] = [];
  selectedProjet?: Projet;

  environnements: Environnement[] = [];
  selectedEnv?: Environnement;

  envApps: EnvApplication[] = [];

  loading = false;

  constructor(
    private route: ActivatedRoute,
    private envTypeService: EnvironmentTypeService,
    private projetService: ProjetService,
    private environnementService: EnvironnementService,
    private envAppService: EnvApplicationService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.typeCode = this.route.snapshot.paramMap.get('code') || '';
    this.loadTypeLabel();
    this.loadProjects();
  }

  // ---------------- TYPE ----------------

  loadTypeLabel(): void {
    this.envTypeService.getAllTypes().subscribe({
      next: (types) => {
        const t = types.find(x => x.code === this.typeCode);
        if (t) {
          this.typeLabel = t.libelle;
        } else {
          this.typeLabel = this.typeCode;
        }
      },
      error: (err: any) => {
        console.error(err);
        this.typeLabel = this.typeCode;
      }
    });
  }

  // ---------------- PROJETS ----------------

  loadProjects(): void {
    this.loading = true;
    this.projetService.getProjectsByEnvironmentType(this.typeCode).subscribe({
      next: (projets: Projet[]) => {
        this.projets = projets;
        this.loading = false;

        if (this.projets.length > 0) {
          this.selectProjet(this.projets[0]);
        }
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des projets.');
        this.loading = false;
      }
    });
  }

  selectProjet(p: Projet): void {
    this.selectedProjet = p;
    this.selectedEnv = undefined;
    this.envApps = [];
    this.environnements = [];

    this.environnementService
      .getEnvironmentsByProjetAndType(p.id, this.typeCode)
      .subscribe({
        next: (envs: Environnement[]) => {
          this.environnements = envs;
          if (this.environnements.length > 0) {
            this.selectEnv(this.environnements[0]);
          }
        },
        error: (err: any) => {
          console.error(err);
          this.toastr.error('Erreur lors du chargement des environnements.');
        }
      });
  }

  // ---------------- ENVIRONNEMENTS ----------------

  selectEnv(env: Environnement): void {
    this.selectedEnv = env;
    this.envApps = [];

    this.envAppService.getApplicationsForEnv(env.id).subscribe({
      next: (apps: EnvApplication[]) => {
        this.envApps = apps;
      },
      error: (err: any) => {
        console.error(err);
        this.toastr.error('Erreur lors du chargement des applications d\'environnement.');
      }
    });
  }

  // ---------------- ACTIONS ADMIN (4 actions) ----------------

  onAction(target: 'TYPE' | 'PROJET' | 'ENV' | 'APP', action: 'CONSULT' | 'CREATE' | 'UPDATE' | 'DELETE', payload?: any): void {
    console.log('ADMIN ACTION', { target, action, payload });
    this.toastr.info(
      `Action ${action} sur ${target}`,
      'TODO: implémenter la logique métier'
    );
  }
}
