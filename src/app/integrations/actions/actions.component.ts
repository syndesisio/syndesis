import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Integration } from '../../model';

@Component({
  selector: 'syndesis-integration-actions',
  templateUrl: './actions.component.html'
})
export class IntegrationActionsComponent {

  @Input() integration: Integration;
  @Output() activate = new EventEmitter<Integration>();
  @Output() deactivate = new EventEmitter<Integration>();
  @Output() delete = new EventEmitter<Integration>();
  @Output() edit = new EventEmitter<Integration>();

  canSeeActions() {
    return this.integration.currentStatus !== 'Deleted';
  }

  canActivate() {
    switch (this.integration.currentStatus) {
      case 'Activated':
      case 'Deleted':
      case 'Draft':
      case 'Pending':
        return false;
      default:
        return true;
    }
  }

  canDeactivate() {
    switch (this.integration.currentStatus) {
      case 'Deactivated':
      case 'Deleted':
      case 'Draft':
      case 'Pending':
        return false;
      default:
        return true;
    }
  }

  canDelete() {
    switch (this.integration.currentStatus) {
      case 'Deleted':
        return false;
      default:
        return true;
    }
  }

  canEdit() {
    switch (this.integration.currentStatus) {
      case 'Deleted':
        return false;
      default:
        return true;
    }
  }

  onActivate() {
    this.activate.emit(this.integration);
  }

  onDeactivate() {
    this.deactivate.emit(this.integration);
  }

  onDelete() {
    this.delete.emit(this.integration);
  }

  onEdit() {
    this.edit.emit(this.integration);
  }

}
