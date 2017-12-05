import { ApplicationRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Integration } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';
import { ModalService } from '../../common/modal/modal.service';
import { log } from '../../logging';

import { NotificationType } from 'patternfly-ng';
import { NotificationService } from 'app/common/ui-patternfly/notification-service';
import { IntegrationSupportService } from 'app/store/integration-support.service';
import { saveAs } from 'file-saver';

export class IntegrationViewBase {
  currentAction: string = undefined;
  selectedIntegration: Integration = undefined;

  constructor(
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    private integrationSupportService: IntegrationSupportService,
  ) {}

  canEdit = int => int.currentStatus !== 'Deleted';
  /* tslint:disable semicolon */
  canActivate = int =>
    int.currentStatus === 'Deactivated' || int.currentStatus === 'Draft';
  /* tslint:enable semicolon */
  canDeactivate = int => int.currentStatus === 'Activated';
  canDelete = int => int.currentStatus !== 'Deleted';

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
            saveAs(value.blob(), integration.name + '-export.zip');
          });
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
      case 'activate':
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
    return this.modalService.show();
  }

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
}
