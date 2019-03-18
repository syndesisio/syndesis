import { Component, Input } from '@angular/core';

import { IntegrationOverview } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-status',
  template: `
    <div class="syndesis-integration-status">
      <!-- In Progress -->
      <div class="status pending" *ngIf="integration.currentState === 'Pending'">
        <syndesis-integration-status-detail [integration]="integration"></syndesis-integration-status-detail>
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
      case 'Error':
        return 'danger';
      default:
        return currentState;
    }
  }
}
