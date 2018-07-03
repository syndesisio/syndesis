import {
  ApplicationRef,
  Component,
  Input,
  OnInit,
  ViewChild
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

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
import { IntegrationStore } from '@syndesis/ui/store';
import {
  Integrations,
  Integration,
  IntegrationActionsService
} from '@syndesis/ui/platform';

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
    public route: ActivatedRoute,
    public router: Router,
    public integrationActionsService: IntegrationActionsService
  ) {}

  get modalTitle() {
    return this.integrationActionsService.getModalTitle();
  }

  get modalMessage() {
    return this.integrationActionsService.getModalMessage();
  }

  get modalType() {
    return this.integrationActionsService.getModalType();
  }

  get modalPrimaryText() {
    return this.integrationActionsService.getModalPrimaryText();
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
