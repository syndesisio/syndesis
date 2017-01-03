/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplatesListComponent } from './list.component';

describe('TemplatesListComponent', () => {
  let component: TemplatesListComponent;
  let fixture: ComponentFixture<TemplatesListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TemplatesListComponent],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplatesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
