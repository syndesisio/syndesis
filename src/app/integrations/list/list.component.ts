import { ApplicationRef, Component, Input, ViewChild } from '@angular/core';
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
import { IntegrationViewBase } from '../components/integrationViewBase.component';
import { ModalService } from '../../common/modal/modal.service';
import { log, getCategory } from '../../logging';

@Component({
  selector: 'syndesis-integrations-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class IntegrationsListComponent extends IntegrationViewBase {
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
  ) {
    super(store, route, router, notificationService, modalService, application);
    this.listConfig = {
      dblClick: false,
      multiSelect: false,
      selectItems: false,
      showCheckbox: false,
    };
  }

  handleClick($event: ListEvent) {
    this.router.navigate(['/integrations', $event.item.id], {
      relativeTo: this.route,
    });
  }

  getActionConfig(integration: Integration): ActionConfig {
    const actionConfig = {
      primaryActions: [],
      moreActions: [
        {
          id: 'view',
          title: 'View',
          tooltip: 'View ${integration.name}',
          visible: true,
        },
        {
          id: 'edit',
          title: 'Edit',
          tooltip: `Edit ${integration.name}`,
          visible: this.canEdit(integration),
        },
        {
          id: 'activate',
          title: 'Activate',
          tooltip: `Activate ${integration.name}`,
          visible: this.canActivate(integration),
        },
        {
          id: 'deactivate',
          title: 'Deactivate',
          tooltip: `Deactivate ${integration.name}`,
          visible: this.canDeactivate(integration),
        },
        {
          id: 'delete',
          title: 'Delete',
          tooltip: `Delete ${integration.name}`,
          visible: this.canDelete(integration),
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
}
