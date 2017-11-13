/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';

import { ModalModule } from 'ngx-bootstrap/modal';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { NotificationModule } from 'patternfly-ng';

import { SyndesisCommonModule } from '../../common/common.module';
import { PatternflyUIModule } from '../../common/ui-patternfly/ui-patternfly.module';
import { IntegrationsListModule } from '../list/list.module';
import { IntegrationsListPage } from './list-page.component';
import { StoreModule } from '../../store/store.module';

xdescribe('IntegrationsListPage', () => {
  let component: IntegrationsListPage;
  let fixture: ComponentFixture<IntegrationsListPage>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [
          SyndesisCommonModule.forRoot(),
          StoreModule,
          RouterTestingModule.withRoutes([]),
          RestangularModule.forRoot(),
          ModalModule.forRoot(),
          TooltipModule.forRoot(),
          TabsModule.forRoot(),
          NotificationModule,
          PatternflyUIModule,
          IntegrationsListModule
        ],
        declarations: [IntegrationsListPage],
        providers: [
          MockBackend,
          { provide: RequestOptions, useClass: BaseRequestOptions },
          {
            provide: Http,
            useFactory: (backend, options) => {
              return new Http(backend, options);
            },
            deps: [MockBackend, RequestOptions]
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(IntegrationsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
