// src/app/environments/components/environment-type-layout/environment-type-layout.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-environment-type-layout',
  standalone: false,
  templateUrl: './environment-type-layout.component.html',
  styleUrls: ['./environment-type-layout.component.scss']
})
export class EnvironmentTypeLayoutComponent implements OnInit {

  typeCode: string = '';
  typeLibelle: string = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.typeCode = params.get('typeCode') || '';
      this.typeLibelle = this.getTypeLibelle(this.typeCode);
    });
  }

  private getTypeLibelle(code: string): string {
    const map: { [key: string]: string } = {
      'edition': 'Environnement d\'Édition',
      'integration': 'Environnement d\'Intégration',
      'client': 'Environnement Client'
    };
    return map[code.toLowerCase()] || code;
  }
}
