import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Router } from '@angular/router';

import { ModalDirective } from 'ngx-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';

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

  @ViewChild('childModal') public childModal: ModalDirective;

  connections: Observable<Connections>;
  @Input() integrations: Integrations;
  @Input() loading: boolean;
  selectedId = undefined;
  selectedIntegration: Integration = undefined;
  currentAction: string = undefined;
  truncateTrail = '…';

  public doughnutChartLabels: string[] = [
    'Active',
    'Draft',
    'Inactive',
  ];

  get doughnutChartData() {
    return [
      this.countActiveIntegrations(),
      this.countDraftIntegrations(),
      this.countInactiveIntegrations(),
    ];
  }

  public doughnutChartType = 'doughnut';
  public doughnutChartLegend = false;
  public doughnutChartColors = [{
    backgroundColor: [
      '#0088CE', // PatternFly Blue 400, Active
      '#EC7A08', // PatternFly Orange 400, Draft
      '#D1D1D1', // PatternFly Black 300, Inactive
    ],
  }];
  public doughnutChartOptions: any = {
    cutoutPercentage: 75,
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


  //-----  Modals ------------------->>

  doAction(action: string, integration: Integration) {
    switch (action) {
      case 'activate':
        return this.activateAction(integration);
      case 'deactivate':
        return this.deactivateAction(integration);
      case 'delete':
        return this.deleteAction(integration);
    }
  }

  // TODO: Refactor into single method for both cases
  // Open modal to confirm activation
  requestActivate(integration: Integration) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration['id']));
    this.selectedIntegration = integration;
    this.showModal('activate');
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integration) {
    log.debugc(() => 'Selected integration for deactivation: ' + JSON.stringify(integration['id']));
    this.selectedIntegration = integration;
    this.showModal('deactivate');
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integration) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration['id']));
    this.hideModal();
    const i = JSON.parse(JSON.stringify(integration));
    i.desiredStatus = 'Activated';
    this.integrationStore.update(i).subscribe(() => {
      const toast = {
        type: 'success',
        title: 'Integration is activating',
        body: 'Please allow a moment for the integration to fully activate.',
      };
      setTimeout(this.popToast(toast), 1000);
    }, (reason: any) => {
      const toast = {
        type: 'error',
        title: 'Failed to activate integration',
        body: 'Error activating integration: ' + reason,
      };
      setTimeout(this.popToast(toast), 1000);
    });
  }

  // Actual activate/deactivate action once the user confirms
  deactivateAction(integration: Integration) {
    log.debugc(() => 'Selected integration for deactivation: ' + JSON.stringify(integration['id']));
    this.hideModal();
    const i = JSON.parse(JSON.stringify(integration));
    i.desiredStatus = 'Deactivated';
    this.integrationStore.update(i).subscribe(() => {
      const toast = {
        type: 'success',
        title: 'Integration is deactivating',
        body: 'Please allow a moment for the integration to be deactivated.',
      };
      setTimeout(this.popToast(toast), 1000);
    }, (reason: any) => {
      const toast = {
        type: 'error',
        title: 'Failed to deactivate integration',
        body: 'Error deactivating integration: ' + reason,
      };
      setTimeout(this.popToast(toast), 1000);
    });
  }

  //-----  Delete ------------------->>

  // Actual delete action once the user confirms
  deleteAction(integration: Integration) {
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration['id']));
    this.hideModal();
    this.integrationStore.delete(integration).subscribe(() => {
      const toast = {
        type: 'success',
        title: 'Delete Successful',
        body: 'Integration successfully deleted.',
      };
      setTimeout(this.popToast(toast), 1000);
    }, (reason: any) => {
      const toast = {
        type: 'error',
        title: 'Failed to delete integration',
        body: 'Error deleting integration: ' + reason,
      };
      setTimeout(this.popToast(toast), 1000);
    });
  }

  // Open modal to confirm delete
  requestDelete(integration: Integration) {
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration['id']));
    this.selectedIntegration = integration;
    this.showModal('delete');
  }

  public showModal(action: string): void {
    this.currentAction = action;
    this.childModal.show();
  }

  public hideModal(): void {
    this.currentAction = undefined;
    this.selectedIntegration = undefined;
    this.childModal.hide();
  }

  //-----  Random Text Stuff --------->>

  getActionTitle() {
    switch (this.currentAction) {
      case 'activate':
        return 'Activation';
      case 'deactivate':
        return 'Deactivation';
      default:
        return 'Deletion';
    }
  }

  getAction() {
    return this.currentAction;
  }

  getActionButtonText() {
    switch (this.currentAction) {
      case 'activate':
        return 'Activate';
      case 'deactivate':
        return 'Deactivate';
      default:
        return 'Delete';
    }
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


  //-----  Icons ------------------->>

  getStart(integration: Integration) {
    return integration.steps[0];
  }

  getFinish(integration: Integration) {
    return integration.steps.slice(-1)[0];
  }


  //-----  Selecting a Connection or Integration ------------------->>

  selectedConnection(connection: Connection) {
    this.router.navigate(['/connections', connection.id]);
  }

  goto(integration: Integration) {
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
  }

}
