import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnvironementListComponent } from './environement-list.component';

describe('EnvironementListComponent', () => {
  let component: EnvironementListComponent;
  let fixture: ComponentFixture<EnvironementListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnvironementListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnvironementListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
