import { Component, Input, Output, OnInit, OnDestroy, EventEmitter } from '@angular/core';

import { moment } from '@syndesis/ui/vendor';
import {
  Connections,
  IntegrationState, Integrations, IntegrationMetrics
} from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-dashboard-metrics',
  templateUrl: './dashboard-metrics.component.html',
  styleUrls: ['./dashboard-metrics.component.scss']
})
export class DashboardMetricsComponent implements OnInit, OnDestroy {
  @Input() connections: Connections; // TODO: Replace by connectionState when the ngrx store supports it
  @Input() integrationState: IntegrationState;
  @Output() refresh = new EventEmitter<any>();

  integrations: Integrations;
  uptimeStart: string;

  private metricsRefreshInterval: any;
  private startDate: moment.Moment;

  get errorIntegrations(): number {
    return this.integrations
      .filter(integration => integration.currentStatus === 'Error')
      .length;
  }

  ngOnInit() {
    this.integrations = this.integrationState.collection;
    this.startDate = moment(this.integrationState.metrics.summary.start);
    this.uptimeStart = this.startDate.format('MMM Do hh:mm'); // eg January 12 8:53 pm

    this.metricsRefreshInterval = setInterval(() => {
      this.refresh.emit(null);
    }, 5000);
  }

  get integrationMetrics(): IntegrationMetrics {
    return this.integrationState.metrics.summary;
  }

  get uptimeTimeSpan(): string {
    return moment.duration(moment().diff(this.startDate)).humanize();
  }

  ngOnDestroy() {
    if (this.metricsRefreshInterval) {
      clearInterval(this.metricsRefreshInterval);
    }
  }
}
