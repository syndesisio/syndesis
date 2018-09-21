import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenApiValidatorComponent } from './validator.component';

describe('OpenApiValidatorComponent', () => {
  let component: OpenApiValidatorComponent;
  let fixture: ComponentFixture<OpenApiValidatorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OpenApiValidatorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenApiValidatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
