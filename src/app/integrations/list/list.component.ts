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
  currentAction: string = undefined;

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() integrations: Integrations;

  @Input() loading: boolean;

  constructor(private store: IntegrationStore, toasterService: ToasterService) {
    this.toasterService = toasterService;
  }

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

  //-----  Activate/Deactivate ------------------->>

  // TODO: Refactor into single method for both cases
  // Open modal to confirm activation
  requestActivate(integration: Integration) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration['id']));
    this.showModal('activate');
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integration) {
    log.debugc(() => 'Selected integration for deactivation: ' + JSON.stringify(integration['id']));
    this.showModal('deactivate');
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integration) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration['id']));
    this.hideModal();
    const i = JSON.parse(JSON.stringify(integration));
    i.desiredStatus = 'Activated';
    this.store.update(i).subscribe(() => {
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
    this.store.update(i).subscribe(() => {
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
    this.store.delete(integration).subscribe(() => {
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
    this.showModal('delete');
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


  //-----  Modals ------------------->>

  public showModal(action: string): void {
    this.currentAction = action;
    this.childModal.show();
  }

  public hideModal(): void {
    this.currentAction = undefined;
    this.childModal.hide();
  }

  //-----  Toast ------------------->>

  // Show toast notification
  popToast(toast) {
    this.toasterService.pop(toast);
  }
}
