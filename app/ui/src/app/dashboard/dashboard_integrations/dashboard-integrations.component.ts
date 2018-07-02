import { Component, Input, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';

import { DonutComponent, DonutConfig } from 'patternfly-ng';

import { log } from '@syndesis/ui/logging';
import {
  Connection,
  Connections,
  IntegrationOverview,
  IntegrationOverviews,
  IntegrationSupportService,
  Integration,
  I18NService
} from '@syndesis/ui/platform';
import { IntegrationStore } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-dashboard-integrations',
  templateUrl: './dashboard-integrations.component.html',
  styleUrls: ['./dashboard-integrations.component.scss']
})
export class DashboardIntegrationsComponent implements OnInit, OnDestroy {
  PENDING: string;
  UNPUBLISHED: string;
  PUBLISHED: string;
  integrationOverviews$: Observable<Integration[]>;
  integrations: Array<IntegrationOverview>;
  loading = true;

  @Input() connections: Connections;
  @Input() connectionsLoading: boolean;

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

  private integrationOverviewsSubscription: Subscription;

  constructor(
    public route: ActivatedRoute,
    private router: Router,
    private integrationStore: IntegrationStore,
    private i18NService: I18NService
  ) {
    this.PUBLISHED = i18NService.localize('integrations.published');
    this.UNPUBLISHED = i18NService.localize('integrations.unpublished');
    this.PENDING = i18NService.localize('integrations.pending');
    this.integrationChartData = [
      [this.PUBLISHED, 0],
      [this.UNPUBLISHED, 0],
      [this.PENDING, 0]
    ];
    this.integrationsChartConfig.colors[this.PUBLISHED] = '#0088CE'; // PatternFly Blue 400
    this.integrationsChartConfig.colors[this.UNPUBLISHED] = '#D1D1D1'; // PatternFly Black 300
    this.integrationsChartConfig.colors[this.PENDING] = '#EDEDED'; // PatternFly Black 200
    this.integrationsChartConfig.donut.title = i18NService.localize(
      'integrations.integrations'
    );
  }

  ngOnInit() {
    this.integrationOverviews$ = this.integrationStore.list;
    this.integrationOverviewsSubscription = this.integrationOverviews$.subscribe(
      integrations => {
        this.integrations = integrations;
        this.loading = false;
        this.integrationChartData = [
          [this.PUBLISHED, this.countActiveIntegrations()],
          [this.UNPUBLISHED, this.countInactiveIntegrations()],
          [this.PENDING, this.countPendingIntegrations()]
        ];
      }
    );
    this.integrationStore.loadAll();
  }

  ngOnDestroy() {
    if (this.integrationOverviewsSubscription) {
      this.integrationOverviewsSubscription.unsubscribe();
    }
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

  //-----  Selecting a Connection or Integration ------------------->>
  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }

  goto(integration: IntegrationOverview) {
    this.router.navigate(
      ['/integration/edit', integration.id, 'save-or-add-step'],
      { relativeTo: this.route }
    );
  }
}
