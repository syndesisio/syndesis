import { ApplicationRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Integration, DRAFT, PENDING, ACTIVE, INACTIVE, UNDEPLOYED } from '@syndesis/ui/integration';
import { IntegrationSupportService } from '../integration-support.service';
import { IntegrationStore } from '@syndesis/ui/store';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import { log } from '@syndesis/ui/logging';

import { NotificationType } from 'patternfly-ng';
import { saveAs } from 'file-saver';

export class IntegrationViewBase {
  currentAction: string = undefined;
  selectedIntegration: Integration = undefined;
  modalTitle: string;
  modalMessage: string;

  constructor(
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    public integrationSupportService: IntegrationSupportService,
  ) {}

  canEdit = int => int.currentStatus !== UNDEPLOYED;
  /* tslint:disable semicolon */
  canActivate = int =>
    int.currentStatus === INACTIVE || int.currentStatus === DRAFT;
  /* tslint:enable semicolon */
  canDeactivate = int => int.currentStatus === ACTIVE || int.currentStatus === PENDING;
  canDelete = int => int.currentStatus !== UNDEPLOYED;

  //----- Actions ------------------->>

  requestAction(action: string, integration: Integration) {
    let request, header, message, danger, reason;
    switch (action) {
      case 'view':
        return this.router.navigate(['/integrations', integration.id]);
      case 'edit':
        return this.router.navigate(['/integrations', integration.id, 'edit']);
      case 'export':
        return this.integrationSupportService
          .exportIntegration(integration.id).toPromise()
          .then(value => {
            saveAs(value, integration.name + '-export.zip');
          });
      case 'replaceDraft':
        header = 'Updating draft';
        message = 'Replacing the current draft of the integration';
        danger = 'Failed to update integration draft';
        reason = 'Error updating integration';
        request = this.requestReplaceDraft(integration);
        break;
      case 'publish':
        header = 'Publishing deployment';
        message =
          'Please allow a moment for the integration to fully activate.';
        danger = 'Failed to publish integration deployment';
        reason = 'Error publishing integration';
        request = this.requestPublish(integration);
        break;
      case 'activate':
        header = 'Integration is activating';
        message =
          'Please allow a moment for the integration to fully activate.';
        danger = 'Failed to activate integration';
        reason = 'Error activating integration';
        request = this.requestActivate(integration);
        break;
      case 'deactivate':
        header = 'Integration is deactivating';
        message =
          'Please allow a moment for the integration to be deactivated.';
        danger = 'Failed to deactivate integration';
        reason = 'Error deactivating integration';
        request = this.requestDeactivate(integration);
        break;
      case 'delete':
        header = 'Delete Successful';
        message = 'Integration successfully deleted.';
        danger = 'Failed to delete integration';
        reason = 'Error deleting integration';
        request = this.requestDelete(integration);
        break;
      default:
        break;
    }
    return request.then(
      modal =>
        modal.result
          ? this.doAction(action, integration)
              .then(_ =>
                this.notificationService.popNotification({
                  type: NotificationType.SUCCESS,
                  header,
                  message
                })
              )
              .catch(error =>
                this.notificationService.popNotification({
                  type: NotificationType.DANGER,
                  header: danger,
                  message: `${reason}: ${error}`
                })
              )
              .then(_ => this.application.tick())
          : false
    );
  }

  doAction(action: string, integration: Integration) {
    switch (action) {
      case 'replaceDraft':
        return this.replaceDraftAction(integration);
      case 'activate':
      case 'publish':
        return this.activateAction(integration);
      case 'deactivate':
        return this.deactivateAction(integration);
      case 'delete':
        return this.deleteAction(integration);
      default:
        break;
    }
  }

  //-----  Activate/Deactivate ------------------->>
  requestReplaceDraft(integration: Integration) {
    this.selectedIntegration = integration;
    return this.showModal('replaceDraft');
  }

  requestPublish(integration: Integration) {
    this.selectedIntegration = integration;
    return this.showModal('publish');
  }

  // TODO: Refactor into single method for both cases
  // Open modal to confirm activation
  requestActivate(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for activation: ' +
        JSON.stringify(integration['id'])
    );
    this.selectedIntegration = integration;
    return this.showModal('activate');
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id'])
    );
    this.selectedIntegration = integration;
    return this.showModal('deactivate');
  }

  // Open modal to confirm delete
  requestDelete(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id'])
    );
    this.selectedIntegration = integration;
    return this.showModal('delete');
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integration): Promise<any> {
    log.debugc(
      () =>
        'Selected integration for activation: ' +
        JSON.stringify(integration['id'])
    );
    return this.store
      .activate(integration)
      .take(1)
      .toPromise();
  }

  // Actual activate/deactivate action once the user confirms
  deactivateAction(integration: Integration): Promise<any> {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id'])
    );
    return this.store
      .deactivate(integration)
      .take(1)
      .toPromise();
  }

  // Actual delete action once the user confirms
  deleteAction(integration: Integration): Promise<any> {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id'])
    );
    return this.store
      .delete(integration)
      .take(1)
      .toPromise();
  }

  replaceDraftAction(integration: Integration): Promise<any> {
    return this.store.update(integration, true).take(1).toPromise();
  }

  //-----  Icons ------------------->>

  getStart(integration: Integration) {
    return integration.steps[0];
  }

  getFinish(integration: Integration) {
    return integration.steps.slice(-1)[0];
  }

  //-----  Modal ------------------->>

  showModal(action: string) {
    this.currentAction = action;
    this.setModalProperties(action);
    return this.modalService.show();
  }

  setModalProperties(action) {
    switch (action) {
      case 'replaceDraft':
        this.modalTitle =  'Confirm Replace Draft';
        this.modalMessage = 'Are you sure you would like to replace the current draft for this integration?';
        break;
      case 'publish':
        this.modalTitle = 'Confirm Publish';
        this.modalMessage = 'Are you sure you would like to publish this deployment?';
        break;
      case 'activate':
        this.modalTitle = 'Confirm Activation';
        this.modalMessage = 'Are you sure you would like to activate the \'' + this.selectedIntegration.name + '\' integration?';
        break;
      case 'deactivate':
        this.modalTitle = 'Confirm Deactivation';
        this.modalMessage = 'Are you sure you would like to deactivate the \'' + this.selectedIntegration.name + '\' integration?';
        break;
      default:
        this.modalTitle = 'Confirm Deletion';
        this.modalMessage = 'Are you sure you would like to delete the \'' + this.selectedIntegration.name + '\' integration?';
    }
  }
}
