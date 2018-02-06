import { ApplicationRef, Component, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs/Subscription';

import {
  Action,
  ActionConfig,
  ListConfig,
  ListEvent,
  EmptyStateConfig,
  Notification,
  NotificationType
} from 'patternfly-ng';

import { log, getCategory } from '@syndesis/ui/logging';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import { IntegrationStore } from '@syndesis/ui/store';
import { Integrations, Integration, IntegrationActionsService, IntegrationSupportService } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class IntegrationListComponent implements OnInit {
  @Input() complete: boolean;
  @Input() integrations: Integrations = [];
  listConfig: ListConfig;

  constructor(
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    public integrationSupportService: IntegrationSupportService,
    public integrationActionsService: IntegrationActionsService
  ) {

  }

  get modalTitle() {
    return this.integrationActionsService.getModalTitle();
  }

  get modalMessage() {
    return this.integrationActionsService.getModalMessage();
  }

  ngOnInit() {
    this.listConfig = {
      dblClick: false,
      multiSelect: false,
      selectItems: false,
      showCheckbox: false,
      emptyStateConfig: {
        iconStyleClass: 'pficon pficon-add-circle-o',
        title: 'Create an integration',
        info:
          'There are currently no integrations available. Please click on the button below to create one.',
        actions: {
          primaryActions: [
            {
              id: 'createIntegration',
              title: 'Create Integration',
              tooltip: 'create an integration'
            }
          ],
          moreActions: []
        } as ActionConfig
      } as EmptyStateConfig
    };
  }

}
