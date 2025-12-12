
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthContextService } from '../../auth/services/auth-context.service';

@Component({
  selector: 'app-environment-type-layout',
  standalone: false,
  templateUrl: './environment-type-layout.component.html',
  styleUrls: ['./environment-type-layout.component.scss']
})
export class EnvironmentTypeLayoutComponent implements OnInit, OnDestroy {

  typeCode: string = '';
  typeLibelle: string = '';

  private readonly destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authContext: AuthContextService
  ) {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.typeCode = params.get('typeCode') || '';
        this.typeLibelle = this.getTypeLibelle(this.typeCode);

        const canView = this.authContext.canViewType(this.typeCode);
        if (!canView) {
          this.router.navigate(['/auth/access-denied']);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private getTypeLibelle(code: string): string {
    const map: { [key: string]: string } = {
      'edition': "Environnement d'Edition",
      'integration': "Environnement d'Integration",
      'client': 'Environnement Client'
    };
    return map[code.toLowerCase()] || code;
  }
}
