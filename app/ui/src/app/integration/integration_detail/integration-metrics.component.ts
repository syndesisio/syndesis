import {
  Component,
  OnInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter
} from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { moment } from '@syndesis/ui/vendor';
import { ConfigService } from '@syndesis/ui/config.service';
import {
  IntegrationState,
  Integration,
  IntegrationMetrics
} from '@syndesis/ui/platform';

const DEFAULT_POLLING_INTERVAL = 5000;

@Component({
  selector: 'syndesis-integration-metrics',
  templateUrl: './integration-metrics.component.html',
  styleUrls: ['./integration-metrics.component.scss']
})
export class IntegrationMetricsComponent implements OnInit, OnDestroy {
  @Input() integration: Integration;
  @Input() integrationMetrics: IntegrationMetrics;
  @Output() refresh = new EventEmitter();

  uptimeStart: string;

  private metricsRefreshInterval: any;

  constructor(private configService: ConfigService) {}

  ngOnInit() {
    this.uptimeStart = moment(this.integrationMetrics.start).format(
      'MMM Do HH:mm A'
    ); // eg January 12nd 8:53 pm

    let pollingInterval: number;

    try {
      pollingInterval = this.configService.getSettings(
        'metricsPollingInterval'
      );
    } catch (error) {
      pollingInterval = DEFAULT_POLLING_INTERVAL;
    }

    if (pollingInterval > 0) {
      this.metricsRefreshInterval = setInterval(
        () => this.refresh.emit(),
        pollingInterval
      );
    }
  }

  ngOnDestroy() {
    if (this.metricsRefreshInterval) {
      clearInterval(this.metricsRefreshInterval);
    }
  }
}
