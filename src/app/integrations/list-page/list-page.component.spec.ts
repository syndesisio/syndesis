/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';

import { ModalModule } from 'ngx-bootstrap/modal';
import { ToasterModule } from 'angular2-toaster';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { TooltipModule } from 'ngx-bootstrap/tooltip';

import { SyndesisCommonModule } from '../../common/common.module';
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
        SyndesisCommonModule,
        StoreModule,
        RouterTestingModule.withRoutes([]),
        RestangularModule.forRoot(),
        ModalModule.forRoot(),
        TooltipModule.forRoot(),
        TabsModule.forRoot(),
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
