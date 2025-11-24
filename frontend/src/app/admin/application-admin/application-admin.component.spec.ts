import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationAdminComponent } from './application-admin.component';

describe('ApplicationAdminComponent', () => {
  let component: ApplicationAdminComponent;
  let fixture: ComponentFixture<ApplicationAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ApplicationAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApplicationAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
