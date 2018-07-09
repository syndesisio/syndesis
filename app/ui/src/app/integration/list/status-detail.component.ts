import { Component, Input, OnInit } from '@angular/core';

import { Integration, IntegrationStatusDetail, I18NService, StringMap } from '@syndesis/ui/platform';
import { ConfigService } from '@syndesis/ui/config.service';

@Component({
  selector: 'syndesis-integration-status-detail',
  template: `
    <ng-container *ngIf="statusDetail">
      <div>
        <i [innerHtml]="statusDetailText"></i>&nbsp;
        <span *ngIf="logUrl" [innerHtml]="'log-link' | synI18n: logUrl"></span>
      </div>
      <div class="progress progress-xs">
        <div class="progress-bar"
              role="progressbar"
              [ngStyle]="barWidth">
              <span class="sr-only">{{ 'bar-width' | synI18n: barWidth.width }}</span>
              </div>
      </div>
    </ng-container>
    <!-- this is a fallback -->
    <ng-container *ngIf="!statusDetail">
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
    </ng-container>
  `,
  styles: [
    `
      progress: {
        padding: 0;
      }
    `
  ]
})
export class IntegrationStatusDetailComponent implements OnInit {
  @Input() integration: Integration;
  statusDetail: IntegrationStatusDetail;
  statusDetailText: string;
  logUrl: string;
  barWidth: StringMap<String> = {
    width: '0%'
  };

  constructor(private i18NService: I18NService,
              private configService: ConfigService) {}

  ngOnInit() {
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
      if (statusDetail.logsUrl) {
        const base = this.configService.getSettings('consoleUrl');
        if (base) {
          const logUrl = statusDetail.logsUrl
            .replace('https://openshift.default.svc/api/v1/namespaces', `${base}/project`)
            .replace('/pods/', '/browse/pods/')
            .replace('/logs', '?tab=logs');
          this.logUrl = logUrl;
        }
      }
    }
  }
}
