import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

import { moment } from '@syndesis/ui/vendor';
import { IntegrationState, Integration, IntegrationMetrics } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-metrics',
  templateUrl: './integration-metrics.component.html',
  styleUrls: ['./integration-metrics.component.scss']
})
export class IntegrationMetricsComponent implements OnInit {
  @Input() integration: Integration;
  @Input() integrationMetrics: IntegrationMetrics;

  uptimeStart: string;
  uptimeLegend$: Observable<string>;

  private startDate: moment.Moment;

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
