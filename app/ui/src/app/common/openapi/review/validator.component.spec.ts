import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenApiReviewComponent } from './review.component';

describe('OpenApiReviewComponent', () => {
  let component: OpenApiReviewComponent;
  let fixture: ComponentFixture<OpenApiReviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OpenApiReviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenApiReviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
