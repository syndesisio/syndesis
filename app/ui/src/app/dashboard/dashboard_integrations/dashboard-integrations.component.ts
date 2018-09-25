import { Component, Input, OnChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { DonutChartConfig as DonutConfig } from 'patternfly-ng';

import {
  Integration,
  Integrations,
  I18NService,
  IntegrationState
} from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-dashboard-integrations',
  templateUrl: './dashboard-integrations.component.html',
  styleUrls: ['./dashboard-integrations.component.scss']
})
export class DashboardIntegrationsComponent implements OnChanges {
  PENDING: string;
  UNPUBLISHED: string;
  PUBLISHED: string;

  @Input() integrations: Integrations;
  @Input() integrationState: IntegrationState;
  @Input() loading: boolean;

  sortedIntegrationsByTimestamp: Integrations;
  sortedIntegrationsByMessageCount: Integrations;
  integrationChartData: any[];

  integrationsChartConfig: DonutConfig = {
    chartHeight: 120,
    chartId: 'integrationsCounter',
    colors: {}, // initialized in constructor
    donut: {}, // initialized in constructor
    legend: {
      show: true,
      position: 'right'
    }
  };

  constructor(
    public route: ActivatedRoute,
    private router: Router,
    private i18NService: I18NService
  ) {
    this.PUBLISHED = this.i18NService.localize('integrations.published');
    this.UNPUBLISHED = this.i18NService.localize('integrations.unpublished');
    this.PENDING = this.i18NService.localize('integrations.pending');
    this.integrationChartData = [
      [this.PUBLISHED, 0],
      [this.UNPUBLISHED, 0],
      [this.PENDING, 0]
    ];
    this.integrationsChartConfig.colors[this.PUBLISHED] = '#0088CE'; // PatternFly Blue 400
    this.integrationsChartConfig.colors[this.UNPUBLISHED] = '#D1D1D1'; // PatternFly Black 300
    this.integrationsChartConfig.colors[this.PENDING] = '#EDEDED'; // PatternFly Black 200
    this.integrationsChartConfig.donut.title = this.i18NService.localize(
      'integrations.integrations'
    );
  }

  ngOnChanges() {
    const byTimestamp = (a, b) => {
      const aTimestamp = this.getTimestamp(a);
      const bTimestamp = this.getTimestamp(b);
      return bTimestamp - aTimestamp;
    };
    const topIntegrations = this.integrationState.metrics.summary.topIntegrations || {};
    const topIntegrationsArray = Object.keys(topIntegrations).map(key => {
      return {
        id: key,
        count: topIntegrations[key]
      } as any;
    }).sort((a, b) => {
      return b.count - a.count;
    });
    this.sortedIntegrationsByTimestamp = this.integrations.concat().sort(byTimestamp);
    this.sortedIntegrationsByMessageCount = this.sortedIntegrationsByTimestamp.concat().sort((a, b) => {
      const index = topIntegrationsArray.findIndex(i => i.id === b.id);
      return index === -1 ? topIntegrationsArray.length + 1 : index;
    }).reverse();
    this.integrationChartData = [
      [this.PUBLISHED, this.countActiveIntegrations()],
      [this.UNPUBLISHED, this.countInactiveIntegrations()],
      [this.PENDING, this.countPendingIntegrations()]
    ];
  }

  getTimestamp(integration: Integration) {
    return integration.updatedAt !== 0 ? integration.updatedAt : integration.createdAt;
  }

  //-----  Integration Board Chart ------------------->>

  filterIntegrations() {
    const active = [];
    const inactive = [];
    const pending = [];
    let total = 0;
    (this.integrations || []).forEach(integration => {
      switch (integration.currentState) {
        case 'Published':
          total = total + 1;
          active.push(integration);
          break;
        case 'Unpublished':
          total = total + 1;
          inactive.push(integration);
          break;
        case 'Pending':
          total = total + 1;
          pending.push(integration);
          break;
        default:
          break;
      }
    });
    return {
      active: active,
      inactive: inactive,
      pending: pending,
      total: total
    };
  }

  countActiveIntegrations() {
    return this.filterIntegrations().active.length;
  }

  countInactiveIntegrations() {
    return this.filterIntegrations().inactive.length;
  }

  countPendingIntegrations() {
    return this.filterIntegrations().pending.length;
  }

  //-----  Recent Updates Section ------------------->>

  getLabelClass(integration): string {
    switch (integration.currentState) {
      case 'Published':
      default:
        return 'label-primary';
      case 'Unpublished':
        return 'label-default';
    }
  }

  goto(integration: Integration) {
    this.router.navigate(
      ['/integration/edit', integration.id, 'save-or-add-step'], {
        relativeTo: this.route,
        fragment: integration.flows[0].id
      }
    );
  }
}
