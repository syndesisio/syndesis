import { Component, Input, OnChanges } from '@angular/core';

import {
  Integration,
  IntegrationStatusDetail,
  I18NService,
  StringMap,
  ConsoleLinkType
} from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

@Component({
  selector: 'syndesis-integration-status-detail',
  template: `
    <div class="integration-status-detail">
      <ng-container *ngIf="statusDetail">
        <div class="statusDetail">
          <div>
            <i [innerHtml]="statusDetailText"></i>&nbsp;
            <span class="pull-right" *ngIf="logUrl" [innerHtml]="'log-link' | synI18n: logUrl"></span>
          </div>
          <div class="progress progress-xs">
            <div class="progress-bar"
                  role="progressbar"
                  [ngStyle]="barWidth">
                  <span class="sr-only">{{ 'bar-width' | synI18n: barWidth.width }}</span>
                  </div>
          </div>
        </div>
      </ng-container>
      <!-- this is a fallback -->
      <ng-container *ngIf="!statusDetail">
        <div class="statusState">
          <div class="spinner spinner-sm spinner-inline"></div>
          <ng-container [ngSwitch]="integration.targetState">
            <ng-container *ngSwitchCase="'Published'">
              {{ 'integrations.publishing' | synI18n }}
            </ng-container>
            <ng-container *ngSwitchCase="'Unpublished'">
              {{ 'integrations.unpublishing' | synI18n }}
            </ng-container>
            <ng-container *ngSwitchDefault>
              {{ 'integrations.pending' | synI18n }}
            </ng-container>
          </ng-container>
        </div>
      </ng-container>
    </div>
  `,
  styles: [
  `
    .integration-status-detail {
      width: 180px;
    }
    .integration-status-detail .statusDetail {
      position: relative;
      transform: translateY(-4px);
    }
    .integration-status-detail .progress.progress-xs {
      position: absolute;
      top: 100%;
      width: 100%;
    }
    .integration-status-detail .statusState {
      display: inline-flex;
      align-items: center;
    }
  `
  ]
})
export class IntegrationStatusDetailComponent implements OnChanges {
  @Input() integration: Integration;
  statusDetail: IntegrationStatusDetail;
  statusDetailText: string;
  logUrl: string;
  barWidth: StringMap<String> = {
    width: '0%'
  };

  constructor(private i18NService: I18NService,
              private configService: ConfigService) {}

  initialize() {
    if (this.integration && this.integration.statusDetail) {
      const statusDetail = this.statusDetail = this.integration.statusDetail;
      const total = statusDetail.detailedState.totalSteps;
      const current = statusDetail.detailedState.currentStep;
      this.statusDetailText =
        this.i18NService.localize('integration-detail-state',
                                  [this.i18NService.localize(statusDetail.detailedState.value),
                                  current,
                                  total]);
      this.barWidth.width = ((current / total) * 100) + '%';
      const consoleUrl = <string> this.configService.getSettings('consoleUrl');
      // Check if the console URL is set properly, if not don't set up the log/event link
      if (!consoleUrl || !consoleUrl.length) {
        return;
      }
      this.logUrl = undefined;
      if (statusDetail.linkType && statusDetail.namespace && statusDetail.podName) {
        switch (statusDetail.linkType) {
          case ConsoleLinkType.Events:
            this.logUrl = this.i18NService.localize('pod-events-url', [statusDetail.namespace, statusDetail.podName]);
            break;
          case ConsoleLinkType.Logs:
            this.logUrl = this.i18NService.localize('pod-logs-url', [statusDetail.namespace, statusDetail.podName]);
            break;
          default:
        }
      }
    }
  }

  ngOnChanges() {
    this.initialize();
  }

}
