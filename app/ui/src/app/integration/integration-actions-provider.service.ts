import { switchMap, take } from 'rxjs/operators';
import { ApplicationRef, Injectable } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  Integration,
  IntegrationDeployment,
  IntegrationOverview,
  DeploymentOverview,
  IntegrationActionsService,
  IntegrationSupportService,
  Step,
  I18NService,
  PENDING,
  UNPUBLISHED,
  PUBLISHED
} from '@syndesis/ui/platform';
import { IntegrationStore } from '@syndesis/ui/store';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import { log } from '@syndesis/ui/logging';

import { NotificationType } from 'patternfly-ng';
import { saveAs } from 'file-saver';

@Injectable()
export class IntegrationActionsProviderService extends IntegrationActionsService {
  currentAction: string;
  selectedIntegration: Integration | IntegrationOverview;
  modalTitle: string;
  modalMessage: string;
  modalType: string;
  modalPrimaryText: string;

  constructor(
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    public integrationSupportService: IntegrationSupportService,
    private i18NService: I18NService
  ) {
    super();
  }

  canActivate(integration: Integration) {
    return integration.currentState !== PENDING && integration.currentState !== PUBLISHED;
  }

  canEdit(integration: Integration) {
    return integration.currentState !== PENDING;
  }

  canDeactivate(integration: Integration) {
    return integration.currentState !== UNPUBLISHED;
  }

  //----- Actions ------------------->>

  requestAction(
    action: string,
    integration: Integration | IntegrationOverview,
    deployment?: IntegrationDeployment | DeploymentOverview
  ) {
    let request, header, message, danger, reason;
    switch (action) {
      case 'createIntegration':
        return this.router.navigate(['/integrations/create']);
      case 'view':
        return this.router.navigate(['/integrations', integration.id]);
      case 'edit':
        return this.router.navigate(['/integrations', integration.id, 'edit']);
      case 'export':
        return this.integrationSupportService
          .exportIntegration(integration.id)
          .toPromise()
          .then(value => {
            saveAs(value, integration.name + '-export.zip');
          });
      case 'replaceDraft':
        header = this.i18NService.localize('updating-draft-header');
        message = this.i18NService.localize('updating-draft-message');
        danger = this.i18NService.localize('updating-draft-danger');
        reason = this.i18NService.localize('updating-draft-reason');
        request = this.requestReplaceDraft(integration, deployment);
        break;
      case 'publish':
        header = this.i18NService.localize('publishing-integration-header');
        message = this.i18NService.localize('publishing-integration-message');
        danger = this.i18NService.localize('publishing-integration-danger');
        reason = this.i18NService.localize('publishing-integration-reason');
        request = this.requestPublish(integration);
        break;
      case 'unpublish':
        header = this.i18NService.localize('unpublishing-integration-header');
        message = this.i18NService.localize('unpublishing-integration-message');
        danger = this.i18NService.localize('unpublishing-integration-danger');
        reason = this.i18NService.localize('unpublishing-integration-reason');
        request = this.requestDeactivate(integration);
        break;
      case 'delete':
        header = this.i18NService.localize('delete-integration-header');
        message = this.i18NService.localize('delete-integration-message');
        danger = this.i18NService.localize('delete-integration-danger');
        reason = this.i18NService.localize('delete-integration-reason');
        request = this.requestDelete(integration);
        break;
      default:
        break;
    }
    return request.then(
      modal =>
        modal.result
          ? this.doAction(action, integration, deployment)
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

  getModalTitle() {
    return this.modalTitle || '';
  }

  getModalMessage() {
    return this.modalMessage || '';
  }

  getModalType() {
    return this.modalType || '';
  }

  getModalPrimaryText() {
    return this.modalPrimaryText || '';
  }

  doAction(
    action: string,
    integration: Integration | IntegrationOverview,
    deployment?: IntegrationDeployment | DeploymentOverview
  ) {
    switch (action) {
      case 'replaceDraft':
        return this.replaceDraftAction(integration, deployment);
      case 'activate':
      case 'publish':
        return this.activateAction(integration);
      case 'unpublish':
        return this.deactivateAction(integration);
      case 'delete':
        return this.deleteAction(integration);
      default:
        break;
    }
  }

  //-----  Activate/Deactivate ------------------->>
  requestReplaceDraft(
    integration: Integration | IntegrationOverview,
    deployment: IntegrationDeployment | DeploymentOverview
  ) {
    this.selectedIntegration = integration;
    return this.showModal('replaceDraft');
  }

  requestPublish(integration: Integration | IntegrationOverview) {
    this.selectedIntegration = integration;
    return this.showModal('publish');
  }

  // TODO: Refactor into single method for both cases
  // Open modal to confirm activation
  requestActivate(integration: Integration | IntegrationOverview) {
    log.debugc(
      () =>
        'Selected integration for activation: ' +
        JSON.stringify(integration['id'])
    );
    this.selectedIntegration = integration;
    return this.showModal('activate');
  }

  // Open modal to confirm deactivation
  requestDeactivate(integration: Integration | IntegrationOverview) {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id'])
    );
    this.selectedIntegration = integration;
    return this.showModal('unpublish');
  }

  // Open modal to confirm delete
  requestDelete(integration: Integration | IntegrationOverview) {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id'])
    );
    this.selectedIntegration = integration;
    return this.showModal('delete');
  }

  // TODO: Refactor into single method for both cases
  // Actual activate/deactivate action once the user confirms
  activateAction(integration: Integration | IntegrationOverview): Promise<any> {
    log.debugc(
      () =>
        'Selected integration for activation: ' +
        JSON.stringify(integration['id'])
    );
    return this.integrationSupportService.deploy(<any>integration).toPromise();
  }

  // Actual activate/deactivate action once the user confirms
  deactivateAction(
    integration: Integration | IntegrationOverview
  ): Promise<any> {
    log.debugc(
      () =>
        'Selected integration for deactivation: ' +
        JSON.stringify(integration['id'])
    );
    return this.integrationSupportService
      .undeploy(<any>integration)
      .toPromise();
  }

  // Actual delete action once the user confirms
  deleteAction(integration: Integration | IntegrationOverview): Promise<any> {
    log.debugc(
      () =>
        'Selected integration for delete: ' + JSON.stringify(integration['id'])
    );
    return this.store
      .delete(<any>integration)
      .pipe(take(1))
      .toPromise();
  }

  replaceDraftAction(
    integration: Integration | IntegrationOverview,
    deployment: IntegrationDeployment | DeploymentOverview
  ): Promise<any> {
    return this.integrationSupportService
      .getDeployment(integration.id, deployment.version.toString())
      .pipe(
        switchMap(_deployment => {
          return this.store.patch(_deployment.integrationId, {
            steps: _deployment.spec.steps
          });
        })
      )
      .toPromise();
  }

  //-----  Icons ------------------->>

  getStart(integration: Integration) {
    return integration.steps ? integration.steps[0] : ({} as Step);
  }

  getFinish(integration: Integration) {
    return integration.steps ? integration.steps.slice(-1)[0] : ({} as Step);
  }

  //-----  Modal ------------------->>

  showModal(action: string) {
    this.currentAction = action;
    this.setModalProperties(action);
    return this.modalService.show();
  }

  setModalProperties(action) {
    this.modalTitle = 'Confirm';
    switch (action) {
      case 'replaceDraft':
        this.modalMessage = this.i18NService.localize('update-draft-modal', [
          this.selectedIntegration.name
        ]);
        this.modalType = '';
        break;
      case 'publish':
        this.modalMessage = this.i18NService.localize(
          'publish-integration-modal',
          [this.selectedIntegration.name]
        );
        this.modalType = '';
        this.modalTitle = this.i18NService.localize('publish-integration-modal-title');
        this.modalPrimaryText = this.i18NService.localize('publish-integration-modal-primary-text');
        break;
      case 'unpublish':
        this.modalMessage = this.i18NService.localize(
          'unpublish-integration-modal',
          [this.selectedIntegration.name]
        );
        this.modalType = '';
        this.modalTitle = this.i18NService.localize('unpublish-integration-modal-title');
        this.modalPrimaryText = this.i18NService.localize('unpublish-integration-modal-primary-text');
        break;
      default:
        this.modalMessage = this.i18NService.localize(
          'delete-integration-modal',
          [this.selectedIntegration.name]
        );
        this.modalType = 'delete';
        this.modalTitle = this.i18NService.localize('delete-integration-modal-title');
    }
  }
}
