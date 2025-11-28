import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnvironmentTypeLayoutComponent } from './environment-type-layout.component';

describe('EnvironmentTypeLayoutComponent', () => {
  let component: EnvironmentTypeLayoutComponent;
  let fixture: ComponentFixture<EnvironmentTypeLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnvironmentTypeLayoutComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnvironmentTypeLayoutComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
