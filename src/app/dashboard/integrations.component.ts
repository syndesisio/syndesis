import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Router } from '@angular/router';

import { log, getCategory } from '../logging';

import { Connection, Connections, Integration, Integrations } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';
import { IntegrationStore } from '../store/integration/integration.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'syndesis-dashboard-integrations',
  templateUrl: './integrations.component.html',
  styleUrls: ['./integrations.component.scss'],
})
export class DashboardIntegrationsComponent implements OnInit {

  @Input() integrations: Integrations;
  @Input() connections: Connections;
  @Input() integrationsLoading: boolean;
  @Input() connectionsLoading: boolean;
  selectedId = undefined;
  truncateTrail = '…';

  public doughnutChartLabels: string[] = ['Active', 'Draft', 'Inactive'];

  get doughnutChartData() {
    return [
      this.countActiveIntegrations(),
      this.countDraftIntegrations(),
      this.countInactiveIntegrations(),
    ];
  }

  public doughnutChartType = 'doughnut';
  public doughnutChartLegend = false;
  public doughnutChartColors = [
    {
      backgroundColor: [
        '#0088CE', // PatternFly Blue 400, Active
        '#EC7A08', // PatternFly Orange 400, Draft
        '#D1D1D1', // PatternFly Black 300, Inactive
      ],
    },
  ];
  public doughnutChartOptions: any = {
    cutoutPercentage: 75,
  };

  constructor(
    public route: ActivatedRoute,
    private router: Router,
  ) {

  }

  //-----  Integration Board Chart ------------------->>

  public filterIntegrations() {
    const active = [];
    const draft = [];
    const inactive = [];
    let total = 0;

    (this.integrations || []).forEach(function(a) {
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
      }
    });
    return {
      active: active,
      draft: draft,
      inactive: inactive,
      total: total,
    };
  }

  public countActiveIntegrations() {
    return this.filterIntegrations().active.length;
  }

  public countDraftIntegrations() {
    return this.filterIntegrations().draft.length;
  }

  public countInactiveIntegrations() {
    return this.filterIntegrations().inactive.length;
  }

  public countTotalIntegrations() {
    return this.filterIntegrations().total;
  }

  public chartClicked(e: any): void {
    //log.debugc(() => 'Click event: ' + JSON.stringify(e));
    log.debugc(() => 'Click event: ' + e);
  }

  public chartHovered(e: any): void {
    log.debugc(() => 'Hover event: ' + JSON.stringify(e));
  }

  //-----  Recent Updates Section ------------------->>

  public getLabelClass(integration): string {
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

  public getStatusText(integration: Integration): string {
    switch (integration.currentStatus) {
      case 'Activated':
        return 'Active';
      case 'Deactivated':
        return 'Inactive';
    }
    return integration.currentStatus;
  }

  //-----  Selecting a Connection or Integration ------------------->>

  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }

  goto(integration: Integration) {
    this.router.navigate(
      ['/integrations/edit', integration.id, 'save-or-add-step'],
      { relativeTo: this.route },
    );
  }


  //-----  Initialization ------------------->>

  ngOnInit() {

  }
}
