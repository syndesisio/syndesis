import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { ModalModule } from 'ngx-bootstrap/modal';
import { TooltipModule } from 'ngx-bootstrap/tooltip';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import {
  ActionModule,
  ListModule,
  ToastNotificationListModule as NotificationModule,
  DonutChartModule as ChartModule
} from 'patternfly-ng';

import { PlatformModule } from '@syndesis/ui/platform';
import { ApiModule } from '@syndesis/ui/api';
import { CoreModule } from '@syndesis/ui/core';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { IntegrationListModule } from '@syndesis/ui/integration';
import { SyndesisStoreModule } from '@syndesis/ui/store';

import { DashboardComponent } from '@syndesis/ui/dashboard/dashboard.component';
import { DashboardConnectionsComponent } from '@syndesis/ui/dashboard/dashboard_connections';
import { DashboardIntegrationsComponent } from '@syndesis/ui/dashboard/dashboard_integrations';
import { DashboardMetricsComponent } from '@syndesis/ui/dashboard/dashboard_metrics';
import { ModalService } from '@syndesis/ui/common/modal';
import { ConfigService } from '@syndesis/ui/config.service';

xdescribe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async(() => {
    const moduleConfig = {
      imports: [
        ApiModule.forRoot(),
        PlatformModule.forRoot(),
        CoreModule.forRoot(),
        SyndesisCommonModule.forRoot(),
        ActionModule,
        ListModule,
        ChartModule,
        ModalModule.forRoot(),
        TooltipModule.forRoot(),
        BsDropdownModule.forRoot(),
        RouterTestingModule.withRoutes([]),
        NotificationModule,
        IntegrationListModule,
        SyndesisStoreModule
      ],
      declarations: [
        DashboardMetricsComponent,
        DashboardComponent,
        DashboardConnectionsComponent,
        DashboardIntegrationsComponent
      ],
      providers: [ConfigService, ModalService]
    };
    TestBed.configureTestingModule(moduleConfig).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
