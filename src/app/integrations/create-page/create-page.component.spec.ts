/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ng2-restangular';

import { RouterModule } from '@angular/router';
import { IPaaSCommonModule } from '../../common/common.module';
import { FlowViewComponent } from './flow-view/flow-view.component';
import { ConnectionsListComponent } from '../../connections/list/list.component';
import { ConnectionsListToolbarComponent } from '../../connections/list-toolbar/list-toolbar.component';
import { StoreModule } from '../../store/store.module';

import { IntegrationsCreatePage } from './create-page.component';

describe('IntegrationsCreateComponent', () => {
  let component: IntegrationsCreatePage;
  let fixture: ComponentFixture<IntegrationsCreatePage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        IPaaSCommonModule,
        StoreModule,
        RouterTestingModule.withRoutes([]),
        RestangularModule.forRoot(),
      ],
      declarations: [
        IntegrationsCreatePage,
        ConnectionsListComponent,
        ConnectionsListToolbarComponent,
        FlowViewComponent,
      ],
      providers: [
        MockBackend,
        {
          provide: RequestOptions,
          useClass: BaseRequestOptions,
        },
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
    fixture = TestBed.createComponent(IntegrationsCreatePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
