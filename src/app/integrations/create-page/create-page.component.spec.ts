/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IntegrationsCreatePage } from './create-page.component';

describe('IntegrationsCreateComponent', () => {
  let component: IntegrationsCreatePage;
  let fixture: ComponentFixture<IntegrationsCreatePage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [IntegrationsCreatePage],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationsCreatePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
