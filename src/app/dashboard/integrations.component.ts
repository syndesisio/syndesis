import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ModalDirective } from 'ng2-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';

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

  private toasterService: ToasterService;
  private toast;

  @ViewChild('childModal') public childModal: ModalDirective;

  connections: Observable<Connections>;
  @Input() integrations: Integrations;
  @Input() loading: boolean;
  selectedId = undefined;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(private connectionStore: ConnectionStore, toasterService: ToasterService) {
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

    //this.store.activate(integration['id']);

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

    //this.store.deactivate(integration['id']);

    this.toast = {
      type: 'success',
      title: 'Integration is deactivating',
      body: 'Please allow a moment for the integration to be deactivated.',
    };

    setTimeout(this.popToast(this.toast), 1000);
  }

  //-----  Selecting an Integration ------------------->>

  @Output() onSelected: EventEmitter<Integration> = new EventEmitter();

  onSelect(integration: Integration) {
    log.debugc(() => 'Selected integration (list): ' + integration.name, category);
    this.selectedId = integration.id;
    this.onSelected.emit(integration);
  }

  isSelected(integration: Integration) {
    return integration.id === this.selectedId;
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


  //-----  Modals ------------------->>

  public showModal(): void {
    this.childModal.show();
  }

  public hideModal(): void {
    this.childModal.hide();
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
