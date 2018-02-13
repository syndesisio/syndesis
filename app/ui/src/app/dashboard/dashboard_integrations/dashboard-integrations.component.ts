import { Component, Input, ViewChild, OnChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { log, getCategory } from '@syndesis/ui/logging';
import { Connection, Connections, IntegrationOverview, IntegrationOverviews } from '@syndesis/ui/platform';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-integrations',
  templateUrl: './dashboard-integrations.component.html',
  styleUrls: ['./dashboard-integrations.component.scss']
})
export class DashboardIntegrationsComponent implements OnChanges {
  chartData: number[];
  @Input() integrations: IntegrationOverviews;
  @Input() connections: Connections;
  @Input() integrationsLoading: boolean;
  @Input() connectionsLoading: boolean;
  selectedId = undefined;
  truncateTrail = '…';

  doughnutChartLabels = ['Published', 'Draft', 'Unpublished'];

  get doughnutChartData() {
    return this.chartData;
  }

  doughnutChartType = 'doughnut';
  doughnutChartLegend = false;
  doughnutChartColors = [
    {
      backgroundColor: [
        '#0088CE', // PatternFly Blue 400, Published
        '#EC7A08', // PatternFly Orange 400, Draft
        '#D1D1D1' // PatternFly Black 300, Unpublished
      ]
    }
  ];
  doughnutChartOptions: any = {
    cutoutPercentage: 75
  };

  constructor(public route: ActivatedRoute, private router: Router) {
    this.chartData = [0, 0, 0];
  }

  ngOnChanges(changes: any) {
    this.chartData[0] = this.countActiveIntegrations();
    this.chartData[1] = this.countDraftIntegrations();
    this.chartData[2] = this.countInactiveIntegrations();
  }

  //-----  Integration Board Chart ------------------->>

  filterIntegrations() {
    const active = [];
    const draft = [];
    const inactive = [];
    let total = 0;
    (this.integrations || []).forEach(integration => {
      switch (integration.currentStatus) {
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
      if (integration.draft) {
        draft.push(integration);
      }
    });
    return {
      active: active,
      draft: draft,
      inactive: inactive,
      total: total
    };
  }

  countActiveIntegrations() {
    return this.filterIntegrations().active.length;
  }

  countDraftIntegrations() {
    return this.filterIntegrations().draft.length;
  }

  countInactiveIntegrations() {
    return this.filterIntegrations().inactive.length;
  }

  countTotalIntegrations() {
    return this.filterIntegrations().total;
  }

  chartClicked(e: any): void {
    log.debugc(() => 'Click event: ' + e);
  }

  chartHovered(e: any): void {
    log.debugc(() => 'Hover event: ' + JSON.stringify(e));
  }

  //-----  Recent Updates Section ------------------->>

  getLabelClass(integration): string {
    switch (integration.currentStatus) {
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
