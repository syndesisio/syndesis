import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StepUploadComponent } from './step-upload.component';

describe('StepUploadComponent', () => {
  let component: StepUploadComponent;
  let fixture: ComponentFixture<StepUploadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StepUploadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StepUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
