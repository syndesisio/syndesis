/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IntegrationsListPage } from './list-page.component';

describe('ListComponent', () => {
  let component: IntegrationsListPage;
  let fixture: ComponentFixture<IntegrationsListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [IntegrationsListPage],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
