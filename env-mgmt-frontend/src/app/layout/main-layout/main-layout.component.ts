import { Component, OnInit } from '@angular/core';
import { AuthContextService } from '../../auth/services/auth-context.service';
import { AuthContext, MenuDTO } from '../../auth/models/auth-context.model';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
  standalone: false
})
export class MainLayoutComponent implements OnInit {

  ctx: AuthContext | null = null;

  constructor(private authCtxService: AuthContextService) {}

  ngOnInit(): void {
    this.authCtxService.authContext$.subscribe(ctx => {
      this.ctx = ctx;
    });
  }

  get menus(): MenuDTO[] {
    return this.ctx?.menus ?? [];
  }

  get username(): string {
    return this.ctx?.username ?? '';
  }
}
