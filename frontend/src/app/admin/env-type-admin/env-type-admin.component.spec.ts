import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnvTypeAdminComponent } from './env-type-admin.component';

describe('EnvTypeAdminComponent', () => {
  let component: EnvTypeAdminComponent;
  let fixture: ComponentFixture<EnvTypeAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnvTypeAdminComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnvTypeAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
