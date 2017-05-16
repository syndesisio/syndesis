import { Component, Input, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';
import { Subscription } from 'rxjs/Subscription';

import { Integrations, Integration } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';

import { log, getCategory } from '../../logging';

@Component({
  selector: 'syndesis-integrations-list',
  templateUrl: './list.component.html',
  styleUrls: [ './list.component.scss' ],
})
export class IntegrationsListComponent {

  private toast;
  currentAction: string = undefined;
  selectedIntegration: Integration = undefined;

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() integrations: Integrations;

  @Input() loading: boolean;

  constructor(public store: IntegrationStore,
              public toasterService: ToasterService,
              public route: ActivatedRoute,
              public router: Router) {

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

  goto(integration: Integration) {
    this.router.navigate([ 'edit', integration.id, 'save-or-add-step' ], { relativeTo: this.route });
  }

  //-----  Activate/Deactivate ------------------->>

  // TODO: Refactor into single method for both cases
  // Open modal to confirm activation
  requestActivate(integration: Integration) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration[ 'id' ]));
    this.selectedIntegration = integration;
    this.showModal('activate');
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integration) {
    log.debugc(() => 'Selected integration for deactivation: ' + JSON.stringify(integration[ 'id' ]));
    this.selectedIntegration = integration;
    this.showModal('deactivate');
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integration) {
    log.debugc(() => 'Selected integration for activation: ' + JSON.stringify(integration[ 'id' ]));
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
    log.debugc(() => 'Selected integration for deactivation: ' + JSON.stringify(integration[ 'id' ]));
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

  // Actual delete action once the user confirms
  deleteAction(integration: Integration) {
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration[ 'id' ]));
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
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration[ 'id' ]));
    this.selectedIntegration = integration;
    this.showModal('delete');
  }


  //-----  Icons ------------------->>

  getStart(integration: Integration) {
    return integration.steps[ 0 ];
  }

  getFinish(integration: Integration) {
    return integration.steps.slice(-1)[ 0 ];
  }


  //-----  Dropdown Action Checks ------------------->>

  canActivate(currentStatus) {
    switch (currentStatus) {
      case 'Activated':
      case 'Deleted':
      case 'Draft':
      case 'Pending':
        return false;
      default:
        return true;
    }
  }

  canDeactivate(currentStatus) {
    switch (currentStatus) {
      case 'Deactivated':
      case 'Deleted':
      case 'Draft':
      case 'Pending':
        return false;
      default:
        return true;
    }
  }

  canDelete(currentStatus) {
    switch (currentStatus) {
      case 'Deleted':
      case 'Pending':
        return false;
      default:
        return true;
    }
  }

  canEdit(currentStatus) {
    switch (currentStatus) {
      case 'Deleted':
        return false;
      default:
        return true;
    }
  }

  //-----  Get Status Icon Class ------------------->>

  getLabelClass(currentStatus) {
    switch (currentStatus) {
      case 'Activated':
        return 'primary';
      case 'Deactivated':
        return 'custom';
      case 'Deleted':
        return 'danger';
      case 'Draft':
        return 'warning';
    }
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

  getStatusText(currentStatus) {
    switch (currentStatus) {
      case 'Activated':
        return 'Active';
      case 'Deactivated':
        return 'Inactive';
      case 'Pending':
        return 'In Progress';
      default:
        return currentStatus;
    }
  }


  //-----  Modals ------------------->>

  public showModal(action: string): void {
    this.currentAction = action;
    this.childModal.show();
  }

  public hideModal(): void {
    this.currentAction = undefined;
    this.selectedIntegration = undefined;
    this.childModal.hide();
  }

  //-----  Toast ------------------->>

  // Show toast notification
  popToast(toast) {
    this.toasterService.pop(toast);
  }
}
