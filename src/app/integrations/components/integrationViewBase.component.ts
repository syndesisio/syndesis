import { ChangeDetectorRef, Input, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { Integrations, Integration } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';
import { ModalService } from '../../common/modal/modal.service';
import { log, getCategory } from '../../logging';

import {
  Action,
  ActionConfig,
  ListConfig,
  ListEvent,
  Notification,
  NotificationService,
  NotificationType,
} from 'patternfly-ng';

export class IntegrationViewBase {

  currentAction: string = undefined;
  selectedIntegration: Integration = undefined;

  canEdit = int => int.currentStatus !== 'Deleted';
  canActivate = int => int.currentStatus === 'Deactivated' || int.currentStatus === 'Draft';
  canDeactivate = int => int.currentStatus === 'Activated';
  canDelete = int => int.currentStatus !== 'Deleted';

  constructor(
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public detector: ChangeDetectorRef,
  ) {}

  //----- Actions ------------------->>
  handleAction($event: Action, integration: Integration) {
    let request;
    switch ($event.id) {
      case 'view':
        return this.router.navigate(['/integrations', integration.id]);
      case 'edit':
        return this.router.navigate(['/integrations', integration.id, 'edit']);
      case 'activate':
        request = this.requestActivate(integration);
        break;
      case 'deactivate':
        request = this.requestDeactivate(integration);
        break;
      case 'delete':
        request = this.requestDelete(integration);
        break;
    }
    return request.then(result => result ? this.doAction($event.id, integration) : false);
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
    log.debugc(
      () =>
        'Selected integration for activation: ' +
        JSON.stringify(integration['id']),
    );
    this.selectedIntegration = integration;
    return this.showModal('activate');
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id']),
    );
    this.selectedIntegration = integration;
    return this.showModal('deactivate');
  }

  // Open modal to confirm delete
  requestDelete(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id']),
    );
    this.selectedIntegration = integration;
    return this.showModal('delete');
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integration, success?: (i: Integration) => void, error?: (reason: any) => void) {
    log.debugc(
      () =>
        'Selected integration for activation: ' +
        JSON.stringify(integration['id']),
    );
    const sub = this.store.activate(integration).subscribe(
      (i) => {
        this.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Integration is activating',
          message:
            'Please allow a moment for the integration to fully activate.',
          showClose: true,
        });
        this.maybeCall(success, i);
        sub.unsubscribe();
        this.detector.markForCheck();
      },
      (reason: any) => {
        this.popNotification({
          type: NotificationType.DANGER,
          header: 'Failed to activate integration',
          message: `Error activating integration: ${reason}`,
          showClose: true,
        });
        this.maybeCall(error, reason);
        sub.unsubscribe();
        this.detector.markForCheck();
      },
    );
  }

  // Actual activate/deactivate action once the user confirms
  deactivateAction(integration: Integration, success?: (i: Integration) => void, error?: (reason: any) => void) {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id']),
    );
    const sub = this.store.deactivate(integration).subscribe(
      (i) => {
        this.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Integration is deactivating',
          message:
            'Please allow a moment for the integration to be deactivated.',
          showClose: true,
        });
        this.maybeCall(success, i);
        sub.unsubscribe();
        this.detector.markForCheck();
      },
      (reason: any) => {
        this.popNotification({
          type: NotificationType.DANGER,
          header: 'Failed to deactivate integration',
          message: `Error deactivating integration: ${reason}`,
          showClose: true,
        });
        this.maybeCall(error, reason);
        sub.unsubscribe();
        this.detector.markForCheck();
      },
    );
  }

  // Actual delete action once the user confirms
  deleteAction(integration: Integration, success?: (i: Integration) => void, error?: (reason: any) => void) {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id']),
    );
    const sub = this.store.delete(integration).subscribe(
      (i) => {
        this.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Delete Successful',
          message: 'Integration successfully deleted.',
          showClose: true,
        });
        this.maybeCall(success, i);
        sub.unsubscribe();
        this.detector.markForCheck();
      },
      (reason: any) => {
        this.popNotification({
          type: NotificationType.DANGER,
          header: 'Failed to delete integration',
          message: `Error deleting integration: ${reason}`,
          showClose: true,
        });
        this.maybeCall(error, reason);
        sub.unsubscribe();
        this.detector.markForCheck();
      },
    );
  }

  maybeCall(func: (arg: any) => void, thing: any) {
    if (func && typeof func === 'function') {
      func(thing);
    }
  }

  //-----  Icons ------------------->>

  getStart(integration: Integration) {
    return integration.steps[0];
  }

  getFinish(integration: Integration) {
    return integration.steps.slice(-1)[0];
  }

  //-----  Modal ------------------->>

  public showModal(action: string) {
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

  //-----  Toast ------------------->>

  // Show toast notification
  popNotification(notification) {
    this.notificationService.message(
      notification.type,
      notification.header,
      notification.message,
      false,
      null,
      [],
    );
  }
}
