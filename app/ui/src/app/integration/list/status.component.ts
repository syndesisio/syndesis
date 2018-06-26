import { Component, Input } from '@angular/core';

import { IntegrationOverview } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-status',
  template: `
    <div class="syndesis-integration-status">
      <!-- In Progress -->
      <div class="status pending" *ngIf="integration.currentState === 'Pending'">
        <div class="spinner spinner-sm spinner-inline"></div>
        <ng-container [ngSwitch]="integration.targetState">
          <ng-container *ngSwitchCase="'Published'">
            {{ 'integrations.publishing' | synI18n }}
          </ng-container>
          <ng-container *ngSwitchCase="'Unpublished'">
            {{ 'integrations.unpublishing' | synI18n }}
          </ng-container>
          <ng-container *ngSwitchDefault>
            {{ 'integrations.pending' | synI18n }}
          </ng-container>
        </ng-container>
      </div>
      <!-- Status -->
      <div *ngIf="integration.currentState !== 'Pending'"
            class="status not-pending">
        <span class="label label-{{ getLabelClass(integration.currentState) }}">
          {{ 'integrations.' + integration.currentState | synI18n }}
        </span>
      </div>
    </div>
  `,
  styleUrls: ['./status.component.scss']
})
export class IntegrationStatusComponent {
  @Input() integration: IntegrationOverview;

  //-----  Get Status Icon Class ------------------->>
  getLabelClass(currentState): string | any {
    switch (currentState) {
      case 'Published':
        return 'primary';
      case 'Unpublished':
        return 'inactive';
      default:
        return currentState;
    }
  }
}
