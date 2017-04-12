import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Router } from '@angular/router';

import { ModalDirective } from 'ng2-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';
import { TooltipDirective } from 'ng2-bootstrap/tooltip';

import { log, getCategory } from '../logging';

import { Connection, Connections, Integration, Integrations } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';
import { IntegrationStore } from '../store/integration/integration.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'ipaas-dashboard-integrations',
  templateUrl: './integrations.component.html',
  styleUrls: ['./integrations.component.scss'],
})
export class DashboardIntegrationsComponent implements OnInit {

  @ViewChild('childModal') public childModal: ModalDirective;

  connections: Observable<Connections>;
  @Input() integrations: Integrations;
  @Input() loading: boolean;
  selectedId = undefined;
  truncateTrail = '…';

  public doughnutChartLabels: string[] = [
    'Active',
    'Deleted',
    'Draft',
    'Inactive',
  ];
  /*
  public doughnutChartData: number[] = [
    //this.countActiveIntegrations(),
    //this.countDeletedIntegrations(),
    //this.countDraftIntegrations(),
    //this.countInactiveIntegrations(),
    1, 0, 3, 2,
  ];
  */

  get doughnutChartData() {
    return [
      this.countActiveIntegrations(),
      this.countDeletedIntegrations(),
      this.countDraftIntegrations(),
      this.countInactiveIntegrations(),
    ];
  }

  public doughnutChartType = 'doughnut';
  public doughnutChartLegend = false;
  public doughnutChartColors = [{
    backgroundColor: [
      '#3f9c35', // PatternFly Green 400, Active
      '#ec7a08', // PatternFly Orange 400, Deleted
      '#0088ce', // PatternFly Blue 400, Draft
      //'#E1E1E1', // PatternFly Custom Gray, Inactive
      //'#d1d1d1', // PatternFly Black 300, Inactive
    ],
  }];
  public doughnutChartOptions: any = {
    cutoutPercentage: 75,
    //legend: {position: 'bottom', fullWidth: false},
  };

  private toasterService: ToasterService;
  private toast;

  constructor(private connectionStore: ConnectionStore,
              private integrationStore: IntegrationStore,
              public route: ActivatedRoute,
              private router: Router,
              toasterService: ToasterService) {
    this.connections = this.connectionStore.list;
    this.toasterService = toasterService;
  }



  //-----  Activate/Deactivate ------------------->>

  // TODO: Refactor into single method for both cases
  // Open modal to confirm activation
  requestActivate(integration: Integrations) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration['id']));
    this.showModal();
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integrations) {
    log.debugc(() => 'Selected integration for deactivation: ' + JSON.stringify(integration['id']));
    this.showModal();
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integrations) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration['id']));

    this.hideModal();

    // Not working yet, we need an `activate` method in the store
    //this.integrationStore.activate(integration['id']);

    this.toast = {
      type: 'success',
      title: 'Integration is activating',
      body: 'Please allow a moment for the integration to fully activate.',
    };

    setTimeout(this.popToast(this.toast), 1000);
  }

  // Actual activate/deactivate action once the user confirms
  deactivateAction(integration: Integrations) {
    log.debugc(() => 'Selected integration for deactivation: ' + JSON.stringify(integration['id']));

    this.hideModal();

    // Not working yet, we need a `deactivate` method in the store
    //this.integrationStore.deactivate(integration['id']);

    this.toast = {
      type: 'success',
      title: 'Integration is deactivating',
      body: 'Please allow a moment for the integration to be deactivated.',
    };

    setTimeout(this.popToast(this.toast), 1000);
  }

  //-----  Icons ------------------->>

  getStartIcon(integration: Integration) {
    const connection = integration.steps[0].connection;
    const icon = 'fa fa-plane';

    return (connection || {})['icon'] || 'fa-plane';
  }

  getFinishIcon(integration: Integration) {
    const connection = integration.steps[integration.steps.length - 1].connection;
    return (connection || {})['icon'] || 'fa-plane';
  }

  //-----  Integration Board Chart ------------------->>

  public filterIntegrations() {
    const active = [];
    const deleted = [];
    const draft = [];
    const inactive = [];

    this.integrations.forEach(function(a) {
      /* TODO - too noisy
      log.debugc(() => 'Integration: ' + JSON.stringify(a));
      log.debugc(() => 'currentStatus: ' + JSON.stringify(a.currentStatus));
      log.debugc(() => 'desiredStatus: ' + JSON.stringify(a.desiredStatus));
      */

      switch (a.desiredStatus) {
        case 'Activated':
          active.push(a);
          break;
        case 'Deleted':
          deleted.push(a);
          break;
        case 'Draft':
          draft.push(a);
          break;
        case 'Deactivated':
          inactive.push(a);
          break;
      }
    });
    return {
      active: active,
      deleted: deleted,
      draft: draft,
      inactive: inactive,
    };
  }

  public countActiveIntegrations() {
    return this.filterIntegrations().active.length;
  }

  public countDeletedIntegrations() {
    return this.filterIntegrations().deleted.length;
  }

  public countDraftIntegrations() {
    return this.filterIntegrations().draft.length;
  }

  public countInactiveIntegrations() {
    return this.filterIntegrations().inactive.length;
  }




  public chartClicked(e: any): void {
    log.debugc(() => 'Click event: ' + JSON.stringify(e));
  }

  public chartHovered(e: any): void {
    log.debugc(() => 'Hover event: ' + JSON.stringify(e));
  }


  //-----  Modals ------------------->>

  public showModal(): void {
    this.childModal.show();
  }

  public hideModal(): void {
    this.childModal.hide();
  }


  //-----  Recent Updates Section ------------------->>

  public getLabelClass(integration): string {
    /* TODO - too noisy
    log.debugc(() => 'Integration: ' + JSON.stringify(integration));
    */
    switch (integration.desiredStatus) {
      case 'Activated':
      default:
        return 'label-success';
      case 'Deactivated':
        return 'label-default';
      case 'Deleted':
        return 'label-danger';
      case 'Draft':
        return 'label-primary';
    }
  }

  //-----  Selecting a Connection or Integration ------------------->>

  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }

  selectedIntegration(integration: Integration) {
    this.router.navigate(['/integrations/edit', integration.id, 'save-or-add-step'], { relativeTo: this.route });
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

  //-----  Toast ------------------->>

  // Show toast notification
  popToast(toast) {
    this.toasterService.pop(toast);
  }

  //-----  Initialization ------------------->>

  ngOnInit() {
    log.debugc(() => 'Got integrations: ' + JSON.stringify(this.integrations, undefined, 2), category);
    this.connectionStore.loadAll();
    //this.filterIntegrations();
  }

}
