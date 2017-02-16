/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ng2-restangular';

import { ModalModule } from 'ng2-bootstrap/modal';
import { ToasterModule } from 'angular2-toaster';

import { IPaaSCommonModule } from '../../common/common.module';
import { IntegrationsListPage } from './list-page.component';
import { IntegrationsListComponent } from '../list/list.component';
import { IntegrationsListToolbarComponent } from '../list-toolbar/list-toolbar.component';
import { StoreModule } from '../../store/store.module';

describe('IntegrationsListPage', () => {
  let component: IntegrationsListPage;
  let fixture: ComponentFixture<IntegrationsListPage>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        IPaaSCommonModule,
        StoreModule,
        RouterTestingModule.withRoutes([]),
        RestangularModule.forRoot(),
        ModalModule,
        ToasterModule,
      ],
      declarations: [
        IntegrationsListPage,
        IntegrationsListComponent,
        IntegrationsListToolbarComponent,
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
    fixture = TestBed.createComponent(IntegrationsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
