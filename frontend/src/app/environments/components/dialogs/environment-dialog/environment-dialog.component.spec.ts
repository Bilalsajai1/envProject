import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnvironmentDialogComponent } from './environment-dialog.component';

describe('EnvironmentDialogComponent', () => {
  let component: EnvironmentDialogComponent;
  let fixture: ComponentFixture<EnvironmentDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnvironmentDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnvironmentDialogComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
