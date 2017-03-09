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

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() integrations: Integrations;

  @Input() loading: boolean;

  constructor(private store: IntegrationStore, toasterService: ToasterService) {
    this.toasterService = toasterService;
  }

  // Actual delete action once the user confirms
  deleteAction(integration: Integrations) {
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration['id']));

    this.hideModal();

    //this.store.deleteEntity(integration['id']);

    setTimeout(this.popToast(), 1000);
  }

  getStartIcon(integration: Integration) {
    const connection = integration.steps[0].connection;
    console.log('Integration: ', integration);
    const icon = 'fa fa-plane';

    return (connection || {})['icon'] || 'fa-plane';
  }

  getFinishIcon(integration: Integration) {
    const connection = integration.steps[integration.steps.length - 1].connection;
    return (connection || {})['icon'] || 'fa-plane';
  }

  // Open modal to confirm delete
  requestDelete(integration: Integrations) {
    log.debugc(() => 'Selected integration for delete: ' + JSON.stringify(integration['id']));
    this.showModal();
  }

  // Show toast notification
  popToast() {
    const toast = {
      type: 'success',
      title: 'Delete Successful',
      body: 'Integration successfully deleted.',
    };

    this.toasterService.pop(toast);
  }

  public showModal(): void {
    this.childModal.show();
  }

  public hideModal(): void {
    this.childModal.hide();
  }
}
