import { Component, Input, OnInit } from '@angular/core';
import { Integration, IntegrationActionsService } from '@syndesis/ui/platform';
import {
  Action, ActionConfig
} from 'patternfly-ng';

@Component({
  selector: 'syndesis-integration-action-menu',
  template: `
    <ng-container *ngIf="actionConfig">
      <pfng-action class="list-pf-actions"
                  [config]="actionConfig"
                  (onActionSelect)="integrationActionsService.requestAction($event.id, integration)">
      </pfng-action>
    </ng-container>
  `
})
export class IntegrationActionMenuComponent implements OnInit {
  @Input() showViewAction = true;
  @Input() showPrimaryActions = false;
  @Input() integration: Integration;
  actionConfig: ActionConfig;

  constructor(public integrationActionsService: IntegrationActionsService) { }

  ngOnInit() {
    this.actionConfig = {
      primaryActions: [],
      moreActions: [],
      moreActionsDisabled: false,
      moreActionsVisible: true
    } as ActionConfig;
    if (this.showViewAction) {
      this.actionConfig.moreActions.push({
        id: 'view',
        title: 'View',
        tooltip: `View ${this.integration.name}`,
      });
    }
    if (!this.showPrimaryActions) {
      this.actionConfig.moreActions.push({
        id: 'edit',
        title: 'Edit',
        tooltip: `Edit ${this.integration.name}`
      });
    }
    this.actionConfig.moreActions.push({
      id: 'publish',
      title: 'Publish',
      tooltip: `Publish ${this.integration.name}`,
      visible: this.integrationActionsService.canActivate(this.integration)

    });
    this.actionConfig.moreActions.push({
      id: 'unpublish',
      title: 'Unpublish',
      tooltip: `Unpublish ${this.integration.name}`,
      visible: this.integrationActionsService.canDeactivate(this.integration)
    });
    this.actionConfig.moreActions.push({
      id: 'delete',
      title: 'Delete',
      tooltip: `Delete ${this.integration.name}`
    });
    this.actionConfig.moreActions.push({
      id: 'export',
      title: 'Export',
      tooltip: `Export ${this.integration.name}`,
    });
    if (this.showPrimaryActions) {
      this.actionConfig.primaryActions.push({
        id: 'edit',
        styleClass: 'btn btn-primary',
        title: 'Edit Integration',
        tooltip: `Edit ${this.integration.name}`
      });
    }
  }
}
