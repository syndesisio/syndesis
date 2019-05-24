import {
  Component,
  Input,
  OnInit
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import {
  ActionConfig,
  ListConfig,
  EmptyStateConfig,
} from 'patternfly-ng';

import {
  IntegrationType,
  Integrations,
  IntegrationActionsService,
  WithId
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
  IntegrationType = IntegrationType;

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

  trackById(index: number, item: WithId): string {
    return item.id;
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
