import { Component, Input, ViewChild } from '@angular/core';
import { ModalDirective } from 'ng2-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';
import { Subscription } from 'rxjs/Subscription';

import { Integrations, Integration } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';

import { log, getCategory } from '../../logging';

@Component({
  selector: 'ipaas-integrations-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class IntegrationsListComponent {

  private toasterService: ToasterService;
  private toast;

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() integrations: Integrations;

  @Input() loading: boolean;

  constructor(private store: IntegrationStore, toasterService: ToasterService) {
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



  //-----  Delete ------------------->>

  // Actual delete action once the user confirms
  deleteAction(integration: Integrations) {
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration['id']));

    this.hideModal();

    //this.store.deleteEntity(integration['id']);

    this.toast = {
      type: 'success',
      title: 'Delete Successful',
      body: 'Integration successfully deleted.',
    };

    setTimeout(this.popToast(this.toast), 1000);
  }

  // Open modal to confirm delete
  requestDelete(integration: Integrations) {
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration['id']));
    this.showModal();
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
}
