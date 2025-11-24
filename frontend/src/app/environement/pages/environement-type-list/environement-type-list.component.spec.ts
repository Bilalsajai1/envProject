import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnvironementTypeListComponent } from './environement-type-list.component';

describe('EnvironementTypeListComponent', () => {
  let component: EnvironementTypeListComponent;
  let fixture: ComponentFixture<EnvironementTypeListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnvironementTypeListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnvironementTypeListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
