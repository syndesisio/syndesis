import { Component, Input, OnInit, EventEmitter, Output, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

import { ConfigService } from '@syndesis/ui/config.service';
import { moment } from '@syndesis/ui/vendor';
import {
  PlatformState, I18NFetch,
  Connections,
  IntegrationState, Integrations, IntegrationMetrics
} from '@syndesis/ui/platform';
import { Store } from '@ngrx/store';

const DEFAULT_POLLING_INTERVAL = 5000;

@Component({
  selector: 'syndesis-dashboard-metrics',
  templateUrl: './dashboard-metrics.component.html',
  styleUrls: ['../dashboard.component.scss',
    './dashboard-metrics.component.scss']
})
export class DashboardMetricsComponent implements OnInit, OnDestroy {
  @Input() connections: Connections; // TODO: Replace by connectionState once the ngrx store supports it
  @Input() integrations: Integrations; // TODO: Replace by integrationState.collection once the legacy Integrations store is phased out
  @Input() integrationState: IntegrationState;
  @Output() refresh = new EventEmitter();

  uptimeStart: string;

  private metricsRefreshInterval: any;

  constructor(private configService: ConfigService, private store: Store<PlatformState>) {}

  get errorIntegrations(): number {
    return this.integrations
      .filter(integration => integration.currentState === 'Error')
      .length;
  }

  get integrationMetrics(): IntegrationMetrics {
    return this.integrationState.metrics.summary;
  }

  ngOnInit() {
    this.uptimeStart = moment(this.integrationMetrics.start).format('MMM Do HH:mm A'); // eg January 12nd 8:53 pm

    let pollingInterval: number;

    try {
      pollingInterval = this.configService.getSettings('metricsPollingInterval');
    } catch (error) {
      pollingInterval = DEFAULT_POLLING_INTERVAL;
    }

    if (pollingInterval > 0) {
      this.metricsRefreshInterval = setInterval(() => this.refresh.emit(), pollingInterval);
    }
  }

  ngOnDestroy() {
    if (this.metricsRefreshInterval) {
      clearInterval(this.metricsRefreshInterval);
    }
  }

  switchLanguage(locale: string): void {
    this.store.dispatch(new I18NFetch(locale));
  }
}
