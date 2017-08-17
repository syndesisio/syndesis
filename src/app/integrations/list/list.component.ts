import { Component, Input, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs/Subscription';

import {
  Action,
  ActionConfig,
  ListConfig,
  ListEvent,
  Notification,
  NotificationService,
  NotificationType,
} from 'patternfly-ng';

import { Integrations, Integration } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';

import { log, getCategory } from '../../logging';

@Component({
  selector: 'syndesis-integrations-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class IntegrationsListComponent {
  @Input() complete: boolean;
  @Input() integrations: Integrations;
  @ViewChild('childModal') public childModal: ModalDirective;
  listConfig: ListConfig;
  currentAction: string = undefined;
  selectedIntegration: Integration = undefined;

  constructor(
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    private notificationService: NotificationService,
  ) {
    this.listConfig = {
      dblClick           : false,
      multiSelect        : false,
      selectItems        : false,
      selectionMatchProp : 'id',
      showCheckbox       : false,
      useExpandItems     : false,
    };
  }

  handleClick($event: ListEvent) {
    this.router.navigate(['/integrations', $event.item.id], { relativeTo: this.route });
  }

  getActionConfig(integration: Integration): ActionConfig {
    const canEdit = (int) => int.currentStatus !== 'Deleted';
    const canActivate = (int) => int.currentStatus === 'Deactivated';
    const canDeactivate = (int) => int.currentStatus === 'Activated';
    const canDelete = (int) => int.currentStatus !== 'Deleted';

    const actionConfig = {
      primaryActions: [],
      moreActions: [
        {
          id      : 'edit',
          title   : 'Edit',
          tooltip : `Edit ${integration.name}`,
          visible : canEdit(integration),
        },
        {
          id      : 'activate',
          title   : 'Activate',
          tooltip : `Activate ${integration.name}`,
          visible : canActivate(integration),
        },
        {
          id      : 'deactivate',
          title   : 'Deactivate',
          tooltip : `Deactivate ${integration.name}`,
          visible : canDeactivate(integration),
        },
        {
          id      : 'delete',
          title   : 'Delete',
          tooltip : `Delete ${integration.name}`,
          visible : canDelete(integration),
        },
      ],
      moreActionsDisabled: false,
      moreActionsVisible: true,
    } as ActionConfig;

    // Hide kebab
    if (integration.currentStatus === 'Deleted') {
      actionConfig.moreActionsVisible = false;
    }

    return actionConfig;
  }

  //----- Actions ------------------->>

  handleAction($event: Action, integration: Integration) {
    switch ($event.id ) {
      case 'view':
        return this.router.navigate(['/integrations', integration.id]);
      case 'edit':
        return this.router.navigate(['/integrations', integration.id, 'edit']);
      case 'activate':
        return this.requestActivate(integration);
      case 'deactivate':
        return this.requestDeactivate(integration);
      case 'delete':
        return this.requestDelete(integration);
    }
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
    this.showModal('activate');
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id']),
    );
    this.selectedIntegration = integration;
    this.showModal('deactivate');
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for activation: ' +
        JSON.stringify(integration['id']),
    );
    this.hideModal();
    const i = JSON.parse(JSON.stringify(integration));
    i.desiredStatus = 'Activated';
    this.store.update(i).subscribe(
      () => {
        setTimeout(this.popNotification(
          {
            type      : NotificationType.SUCCESS,
            header    : 'Integration is activating',
            message   : 'Please allow a moment for the integration to fully activate.',
            showClose : true,
          },
        ), 1000);
      },
      (reason: any) => {
        setTimeout(this.popNotification(
          {
            type      : NotificationType.DANGER,
            header    : 'Failed to activate integration',
            message   : `Error activating integration: ${reason}`,
            showClose : true,
          },
        ), 1000);
      },
    );
  }

  // Actual activate/deactivate action once the user confirms
  deactivateAction(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id']),
    );
    this.hideModal();
    const i = JSON.parse(JSON.stringify(integration));
    i.desiredStatus = 'Deactivated';
    this.store.update(i).subscribe(
      () => {
        setTimeout(this.popNotification(
          {
            type      : NotificationType.SUCCESS,
            header    : 'Integration is deactivating',
            message   : 'Please allow a moment for the integration to be deactivated.',
            showClose : true,
          },
        ), 1000);
      },
      (reason: any) => {
        setTimeout(this.popNotification(
          {
            type      : NotificationType.DANGER,
            header    : 'Failed to deactivate integration',
            message   : `Error deactivating integration: ${reason}`,
            showClose : true,
          },
        ), 1000);
      },
    );
  }

  // Actual delete action once the user confirms
  deleteAction(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id']),
    );
    this.hideModal();
    this.store.delete(integration).subscribe(
      () => {
        setTimeout(this.popNotification(
          {
            type      : NotificationType.SUCCESS,
            header    : 'Delete Successful',
            message   : 'Integration successfully deleted.',
            showClose : true,
          },
        ), 1000);
      },
      (reason: any) => {
        setTimeout(this.popNotification(
          {
            type      : NotificationType.DANGER,
            header    : 'Failed to delete integration',
            message   : `Error deleting integration: ${reason}`,
            showClose : true,
          },
        ), 1000);
      },
    );
  }

  // Open modal to confirm delete
  requestDelete(integration: Integration) {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id']),
    );
    this.selectedIntegration = integration;
    this.showModal('delete');
  }

  //-----  Icons ------------------->>

  getStart(integration: Integration) {
    return integration.steps[0];
  }

  getFinish(integration: Integration) {
    return integration.steps.slice(-1)[0];
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

  //-----  Modal ------------------->>

  public showModal(action: string): void {
    this.currentAction = action;
    this.childModal.show();
  }

  public hideModal(): void {
    this.currentAction = undefined;
    this.selectedIntegration = undefined;
    this.childModal.hide();
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
