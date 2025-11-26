import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoleAdminComponent } from './role-admin.component';

describe('RoleAdminComponent', () => {
  let component: RoleAdminComponent;
  let fixture: ComponentFixture<RoleAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RoleAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoleAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
