/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { StoreModule } from '@ngrx/store';

import { IntegrationsListPage } from './list-page.component';
import { IntegrationsListComponent } from '../list/list.component';
import { IntegrationsListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { reducers } from '../../store/store';

describe('IntegrationsListPage', () => {
  let component: IntegrationsListPage;
  let fixture: ComponentFixture<IntegrationsListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.provideStore(reducers), RouterTestingModule.withRoutes([])],
      declarations: [IntegrationsListPage, IntegrationsListComponent, IntegrationsListToolbarComponent],
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
