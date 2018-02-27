/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MockBackend } from '@angular/http/testing';
import { RequestOptions, BaseRequestOptions, Http } from '@angular/http';
import { RestangularModule } from 'ngx-restangular';
import { HttpClientModule } from '@angular/common/http';
import { StoreModule, Store } from '@ngrx/store';

import { ModalModule } from 'ngx-bootstrap/modal';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ActionModule, ListModule, NotificationModule, ChartModule } from 'patternfly-ng';

import { platformReducer } from '@syndesis/ui/platform';
import { ApiModule } from '@syndesis/ui/api';
import { CoreModule } from '@syndesis/ui/core';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationListModule } from '@syndesis/ui/integration/list';
import { DashboardComponent } from './dashboard.component';
import { DashboardEmptyComponent } from './dashboard_empty';
import { DashboardConnectionsComponent } from './dashboard_connections';
import { DashboardIntegrationsComponent } from './dashboard_integrations';
import { DashboardMetricsComponent } from './dashboard_metrics';
import { IntegrationListComponent } from '@syndesis/ui/integration/list/list.component';
import { IntegrationStatusComponent } from '@syndesis/ui/integration/list/status.component';
import { LoadingComponent } from '@syndesis/ui/common/loading/loading.component';
import { IconPathPipe } from '@syndesis/ui/common/icon-path.pipe.ts';
import { TruncateCharactersPipe } from '@syndesis/ui/common/truncate-characters.pipe';
import { ModalComponent, ModalService } from '@syndesis/ui/common/modal';
import { ConfigService } from '@syndesis/ui/config.service';
import { IntegrationActionMenuComponent } from '@syndesis/ui/integration/list/action-menu.component.ts';
import { StoreModule as LegacyStore } from '@syndesis/ui/store';

xdescribe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(
    async(() => {
      const moduleConfig = {
        imports: [
          ApiModule.forRoot(),
          CoreModule.forRoot(),
          LegacyStore,
          HttpClientModule,
          ActionModule,
          ListModule,
          ChartModule,
          StoreModule.forRoot(platformReducer),
          ModalModule.forRoot(),
          TooltipModule.forRoot(),
          BsDropdownModule.forRoot(),
          StoreModule,
          RouterTestingModule.withRoutes([]),
          RestangularModule,
          NotificationModule,
          IntegrationListModule,
          SyndesisCommonModule
        ],
        declarations: [
          DashboardMetricsComponent,
          DashboardComponent,
          DashboardEmptyComponent,
          DashboardConnectionsComponent,
          DashboardIntegrationsComponent
        ],
        providers: [
          ConfigService,
          ModalService,
          MockBackend,
          Store,
          { provide: RequestOptions, useClass: BaseRequestOptions },
          {
            provide: Http,
            useFactory: (backend, options) => {
              return new Http(backend, options);
            },
            deps: [MockBackend, RequestOptions]
          }
        ]
      };
      TestBed.configureTestingModule(moduleConfig).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
