import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenApiEditorComponent } from './editor.component';

describe('OpenApiEditorComponent', () => {
  let component: OpenApiEditorComponent;
  let fixture: ComponentFixture<OpenApiEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OpenApiEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenApiEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
