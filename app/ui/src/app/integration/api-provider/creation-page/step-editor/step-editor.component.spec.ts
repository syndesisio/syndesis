import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StepEditorComponent } from './step-editor.component';

describe('StepEditorComponent', () => {
  let component: StepEditorComponent;
  let fixture: ComponentFixture<StepEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StepEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StepEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
