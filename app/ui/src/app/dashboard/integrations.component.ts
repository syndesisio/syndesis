import { Component, Input, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Router } from '@angular/router';

import { log, getCategory } from '../logging';

import { Connection, Connections, Integration, Integrations } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';
import { IntegrationStore } from '../store/integration/integration.store';
import { OnChanges } from '@angular/core/src/metadata/lifecycle_hooks';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-integrations',
  templateUrl: './integrations.component.html',
  styleUrls: ['./integrations.component.scss']
})
export class DashboardIntegrationsComponent implements OnChanges {
  chartData: number[];
  @Input() integrations: Integrations;
  @Input() connections: Connections;
  @Input() integrationsLoading: boolean;
  @Input() connectionsLoading: boolean;
  selectedId = undefined;
  truncateTrail = '…';

  doughnutChartLabels: string[] = ['Active', 'Draft', 'Inactive'];

  get doughnutChartData() {
    return this.chartData;
  }

  doughnutChartType = 'doughnut';
  doughnutChartLegend = false;
  doughnutChartColors = [
    {
      backgroundColor: [
        '#0088CE', // PatternFly Blue 400, Active
        '#EC7A08', // PatternFly Orange 400, Draft
        '#D1D1D1' // PatternFly Black 300, Inactive
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
    (this.integrations || []).forEach(function(a) {
      /* TODO - too noisy
      log.debugc(() => 'Integration: ' + JSON.stringify(a));
      log.debugc(() => 'currentStatus: ' + JSON.stringify(a.currentStatus));
      log.debugc(() => 'desiredStatus: ' + JSON.stringify(a.desiredStatus));
      */

      switch (a.currentStatus) {
        case 'Activated':
          total = total + 1;
          active.push(a);
          break;
        case 'Draft':
          total = total + 1;
          draft.push(a);
          break;
        case 'Deactivated':
          total = total + 1;
          inactive.push(a);
          break;
        default:
          break;
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
    //log.debugc(() => 'Click event: ' + JSON.stringify(e));
    log.debugc(() => 'Click event: ' + e);
  }

  chartHovered(e: any): void {
    log.debugc(() => 'Hover event: ' + JSON.stringify(e));
  }

  //-----  Recent Updates Section ------------------->>

  getLabelClass(integration): string {
    /* TODO - too noisy
    log.debugc(() => 'Integration: ' + JSON.stringify(integration));
    */
    switch (integration.currentStatus) {
      case 'Activated':
      default:
        return 'label-primary';
      case 'Deactivated':
        return 'label-default';
      case 'Draft':
        return 'label-warning';
    }
  }

  getStatusText(integration: Integration): string {
    switch (integration.currentStatus) {
      case 'Activated':
        return 'Active';
      case 'Deactivated':
        return 'Inactive';
      default:
        return integration.currentStatus;
    }
  }

  //-----  Selecting a Connection or Integration ------------------->>

  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }

  goto(integration: Integration) {
    this.router.navigate(
      ['/integrations/edit', integration.id, 'save-or-add-step'],
      { relativeTo: this.route }
    );
  }

  //-----  Times Used ------------------->>

  randomizeTimesUsed(integration: Integration) {
    // For testing purposes only
    /*
    if (!integration.timesUsed) {
      log.debugc(() => 'No times used available, auto-generating one..');
      return Math.floor(Math.random() * 25) + 1;
    } else {
      log.debugc(() => 'Times used: ' + JSON.stringify(integration['timesUsed']));
      return integration.timesUsed;
    }
    */
  }
}
