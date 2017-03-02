/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ng2-restangular';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ConnectionViewWrapperComponent } from './view-wrapper.component';
import { ConnectionViewToolbarComponent } from '../view-toolbar/view-toolbar.component';
import { ConnectionsListComponent } from '../list/list.component';
import { ConnectionsListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { ConnectionViewComponent } from '../view/view.component';
import { StoreModule } from '../../store/store.module';
import { IPaaSCommonModule } from '../../common/common.module';

describe('ConnectionViewWrapperComponent', () => {
  let component: ConnectionViewWrapperComponent;
  let fixture: ComponentFixture<ConnectionViewWrapperComponent>;

  beforeEach(async(() => {
    TestBed
      .configureTestingModule({
        imports: [
          CommonModule,
          FormsModule,
          IPaaSCommonModule,
          StoreModule,
          RouterTestingModule.withRoutes([]),
          RestangularModule.forRoot(),
        ],
        declarations: [
          ConnectionViewWrapperComponent,
          ConnectionViewToolbarComponent,
          ConnectionViewComponent,
          ConnectionsListComponent,
          ConnectionsListToolbarComponent,
        ],
        providers: [
          MockBackend,
          { provide: RequestOptions, useClass: BaseRequestOptions },
          {
            provide: Http, useFactory: (backend, options) => {
              return new Http(backend, options);
            }, deps: [MockBackend, RequestOptions],
          },
        ],
      })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectionViewWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => { expect(component).toBeTruthy(); });
});
