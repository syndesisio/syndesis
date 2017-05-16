/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';

import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';

import { ModalModule } from 'ngx-bootstrap/modal';
import { ToasterModule } from 'angular2-toaster';

import { ConnectionViewPage } from './view-page.component';
import { ConnectionViewWrapperComponent } from '../view-wrapper/view-wrapper.component';
import { ConnectionViewToolbarComponent } from '../view-toolbar/view-toolbar.component';
import { ConnectionsListComponent } from '../list/list.component';
import { ConnectionsListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { ConnectionViewComponent } from '../view/view.component';
import { StoreModule } from '../../store/store.module';
import { SyndesisCommonModule } from '../../common/common.module';
import { CurrentConnectionService } from '../create-page/current-connection';

describe('ConnectionViewPage', () => {
  let component: ConnectionViewPage;
  let fixture: ComponentFixture<ConnectionViewPage>;

  beforeEach(async(() => {
    TestBed
      .configureTestingModule({
        imports: [
          CommonModule,
          FormsModule,
          ReactiveFormsModule,
          DynamicFormsCoreModule.forRoot(),
          DynamicFormsBootstrapUIModule,
          SyndesisCommonModule,
          StoreModule,
          RouterTestingModule.withRoutes([]),
          RestangularModule.forRoot(),
          ModalModule,
          ToasterModule,
        ],
        declarations: [
          ConnectionViewPage,
          ConnectionViewWrapperComponent,
          ConnectionViewToolbarComponent,
          ConnectionViewComponent,
          ConnectionsListComponent,
          ConnectionsListToolbarComponent,
        ],
        providers: [
          MockBackend,
          CurrentConnectionService,
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
    fixture = TestBed.createComponent(ConnectionViewPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => { expect(component).toBeTruthy(); });
});
