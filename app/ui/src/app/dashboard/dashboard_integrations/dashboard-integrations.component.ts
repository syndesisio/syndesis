import { Component, Input, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { DonutComponent, DonutConfig } from 'patternfly-ng';

import { log } from '@syndesis/ui/logging';
import {
  Connection,
  Connections,
  IntegrationOverview,
  IntegrationOverviews,
  IntegrationSupportService
} from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-dashboard-integrations',
  templateUrl: './dashboard-integrations.component.html',
  styleUrls: ['./dashboard-integrations.component.scss']
})
export class DashboardIntegrationsComponent implements OnInit, OnDestroy {

  integrationOverviews$: Observable<IntegrationOverviews>;
  integrations: Array<IntegrationOverview>;
  loading = true;

  @Input() connections: Connections;
  @Input() connectionsLoading: boolean;

  integrationChartData: any[] = [
    ['Published', 0],
    ['Unpublished', 0]
  ];

  integrationsChartConfig: DonutConfig = {
    chartHeight: 120,
    chartId: 'integrationsCounter',
    colors: {
      Published: '#0088CE',   // PatternFly Blue 400, Published
      Unpublished: '#D1D1D1'  // PatternFly Black 300, Unpublished
    },
    donut: {
      title: 'Integrations'
    },
    legend: {
      show: true,
      position: 'right'
    }
  };

  private integrationOverviewsSubscription: Subscription;

  constructor(
    public route: ActivatedRoute,
    private router: Router,
    private integrationSupportService: IntegrationSupportService
  ) {
  }

  ngOnInit() {
    this.integrationOverviews$ = this.integrationSupportService.watchOverviews();
    this.integrationOverviewsSubscription = this.integrationOverviews$.subscribe(integrations => {
      this.integrations = integrations;
      this.loading = false;
      this.integrationChartData = [
        [`Published`, this.countActiveIntegrations()],
        [`Unpublished`, this.countInactiveIntegrations()]
      ];
    });
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
        default:
          break;
      }
    });
    return {
      active: active,
      inactive: inactive,
      total: total
    };
  }

  countActiveIntegrations() {
    return this.filterIntegrations().active.length;
  }

  countInactiveIntegrations() {
    return this.filterIntegrations().inactive.length;
  }

  //-----  Recent Updates Section ------------------->>

  getLabelClass(integration): string {
    switch (integration.currentState) {
      case 'Published':
      default:
        return 'label-primary';
      case 'Unpublished':
        return 'label-default';
      case 'Draft':
        return 'label-warning';
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
