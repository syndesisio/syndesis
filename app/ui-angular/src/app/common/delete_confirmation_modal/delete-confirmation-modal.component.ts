import { Component, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'delete-confirmation-modal',
  template: `
  <div class="modal-header">
    <button type="button"
            class="close"
            aria-hidden="true"
            (click)="onModalClick(false)">
      <span class="pficon pficon-close"></span>
    </button>
    <h4 class="modal-title">{{ title }}</h4>
  </div>
  <div class="modal-body">
    <span aria-hidden="true" class="pficon pficon-error-circle-o"></span>
    <div>
      <ng-content></ng-content>
    </div>
  </div>
  <div class="modal-footer">
    <button type="button"
            class="btn btn-default"
            (click)="onModalClick(false)">
      Cancel
    </button>
    <button type="button"
            class="btn btn-danger"
            (click)="onModalClick(true)">
      Delete
    </button>
  </div>
  `
})
export class DeleteConfirmationModalComponent {
  @Input() title = 'Warning';
  @Output() delete = new EventEmitter<boolean>();

  onModalClick(doDelete: boolean): void {
    this.delete.emit(doDelete);
  }
}
