import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StepValidateComponent } from './step-validate.component';

describe('StepValidateComponent', () => {
  let component: StepValidateComponent;
  let fixture: ComponentFixture<StepValidateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StepValidateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StepValidateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
