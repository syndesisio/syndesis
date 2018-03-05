import { ConfigService } from '@syndesis/ui/config.service';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IntegrationState } from '@syndesis/ui/platform';
import { CoreModule } from '@syndesis/ui/core';
import { DashboardMetricsComponent } from './dashboard-metrics.component';
import { DurationPipe } from '@syndesis/ui/common';

describe('DashboardMetricsComponent', () => {
  let component: DashboardMetricsComponent;
  let fixture: ComponentFixture<DashboardMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [CoreModule.forRoot()],
      providers: [ConfigService],
      declarations: [
        DashboardMetricsComponent,
        DurationPipe
      ]
    })
    .compileComponents();
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
          errors: 13
        }
      }
    } as IntegrationState;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
