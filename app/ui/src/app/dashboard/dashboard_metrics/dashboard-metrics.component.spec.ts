import { ConfigService } from '@syndesis/ui/config.service';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CoreModule } from '@syndesis/ui/core';
import { SyndesisCommonModule } from '@syndesis/ui/common';
import { PlatformModule, IntegrationState } from '@syndesis/ui/platform';
import { SyndesisStoreModule } from '@syndesis/ui/store';

import { DashboardMetricsComponent } from '@syndesis/ui/dashboard/dashboard_metrics/dashboard-metrics.component';
import { ApiModule } from '@syndesis/ui/api';
import { EVENTS_SERVICE_MOCK_PROVIDER } from '@syndesis/ui/store/entity/events.service.spec';

describe('DashboardMetricsComponent', () => {
  let component: DashboardMetricsComponent;
  let fixture: ComponentFixture<DashboardMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ApiModule.forRoot(),
        CoreModule.forRoot(),
        PlatformModule.forRoot(),
        SyndesisCommonModule.forRoot(),
        SyndesisStoreModule,
      ],
      providers: [ConfigService, EVENTS_SERVICE_MOCK_PROVIDER],
      declarations: [DashboardMetricsComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardMetricsComponent);
    component = fixture.componentInstance;

    component.connections = [];
    component.integrations = [];
    component.integrationState = {
      collection: [],
      metrics: {
        summary: {
          start: 0,
          messages: 24,
          errors: 13,
        },
      },
    } as IntegrationState;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
