import { Component, Input, Output, EventEmitter } from '@angular/core';

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
    <h4 class="modal-title">Delete Warning</h4>
  </div>
  <div class="modal-body">
    <div class="row">
      <div class="col-xs-12">
        <ng-content></ng-content>
      </div>
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
  @Output() delete = new EventEmitter<boolean>();

  onModalClick(doDelete: boolean): void {
    this.delete.emit(doDelete);
  }
}
