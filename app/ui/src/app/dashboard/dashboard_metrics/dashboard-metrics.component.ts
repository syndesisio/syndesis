import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

import { moment } from '@syndesis/ui/vendor';
import {
  Connections,
  IntegrationState, Integrations, IntegrationMetrics
} from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-dashboard-metrics',
  templateUrl: './dashboard-metrics.component.html',
  styleUrls: ['../dashboard.component.scss',
    './dashboard-metrics.component.scss']
})
export class DashboardMetricsComponent implements OnInit {
  @Input() connections: Connections; // TODO: Replace by connectionState once the ngrx store supports it
  @Input() integrations: Integrations; // TODO: Replace by integrationState.collection once the legacy Integrations store is phased out
  @Input() integrationState: IntegrationState;

  uptimeStart: string;
  uptimeLegend$: Observable<string>;

  private startDate: moment.Moment;

  get errorIntegrations(): number {
    return this.integrations
      .filter(integration => integration.currentState === 'Error')
      .length;
  }

  get integrationMetrics(): IntegrationMetrics {
    return this.integrationState.metrics.summary;
  }

  ngOnInit() {
    this.startDate = moment(this.integrationMetrics.start);
    this.uptimeStart = this.startDate.format('MMM Do HH:mm A'); // eg January 12nd 8:53 pm

    const uptimeDuration = moment.duration(moment().diff(this.startDate));
    this.uptimeLegend$ = Observable.of(uptimeDuration).pipe(
      map(duration => ({
        days: duration.days(),
        hours: duration.hours(),
        minutes: duration.minutes(),
      })),
      map(durationAsObject => Object
        .keys(durationAsObject)
        .reduce((timeSpan: string, key: string) => {
          return durationAsObject[key] > 0 ? timeSpan + `${durationAsObject[key]} ${key} ` : timeSpan;
        }, ''))
    );
  }
}
