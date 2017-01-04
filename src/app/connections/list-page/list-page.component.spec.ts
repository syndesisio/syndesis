/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { StoreModule } from '@ngrx/store';

import { IPaaSCommonModule } from '../../common/common.module';
import { ConnectionsListPage } from './list-page.component';
import { ConnectionsListComponent } from '../list/list.component';
import { ConnectionsListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { reducers } from '../../store/store';

describe('ConnectionListPage', () => {
  let component: ConnectionsListPage;
  let fixture: ComponentFixture<ConnectionsListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [IPaaSCommonModule, StoreModule.provideStore(reducers), RouterTestingModule.withRoutes([])],
      declarations: [ConnectionsListPage, ConnectionsListComponent, ConnectionsListToolbarComponent],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
