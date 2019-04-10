import {
  Component,
  OnInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter
} from '@angular/core';

import { ConfigService } from '@syndesis/ui/config.service';
import {
  Integration,
  IntegrationMetrics
} from '@syndesis/ui/platform';
import { DatePipe } from '@syndesis/ui/common';

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
    this.uptimeStart = new DatePipe().transform(this.integrationMetrics.start);

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
