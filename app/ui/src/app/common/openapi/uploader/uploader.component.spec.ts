import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenApiUploaderComponent } from './uploader.component';

describe('OpenApiUploaderComponent', () => {
  let component: OpenApiUploaderComponent;
  let fixture: ComponentFixture<OpenApiUploaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OpenApiUploaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenApiUploaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
