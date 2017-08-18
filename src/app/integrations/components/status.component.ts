import { Component, OnInit, Input } from '@angular/core';

import { Integration } from '../../model';

@Component({
  selector: 'syndesis-integration-status',
  template: `
    <div class="syndesis-integration-status">
      <!-- In Progress -->
      <div *ngIf="integration.currentStatus === 'Pending'">
        <div class="spinner spinner-sm spinner-inline"></div>
        In Progress
      </div>
      <!-- Status -->
      <div *ngIf="integration.currentStatus !== 'Pending'"
            class="not-pending">
        <span class="label label-{{ getLabelClass(integration.currentStatus) }}">
          {{ getStatusText(integration.currentStatus) }}
        </span>
      </div>
    </div>
  `,
  styleUrls: ['./status.component.scss'],
})
export class IntegrationStatusComponent implements OnInit {
  @Input() integration: Integration;

  constructor() {}

  //-----  Get Status Icon Class ------------------->>

  getLabelClass(currentStatus) {
    switch (currentStatus) {
      case 'Activated':
        return 'primary';
      case 'Deactivated':
        return 'inactive';
      case 'Deleted':
        return 'danger';
      case 'Draft':
        return 'warning';
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

  ngOnInit() {}
}
