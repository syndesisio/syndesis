/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplatesListPage } from './list-page.component';

describe('TemplatesListPageComponent', () => {
  let component: TemplatesListPage;
  let fixture: ComponentFixture<TemplatesListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TemplatesListPage],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplatesListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
