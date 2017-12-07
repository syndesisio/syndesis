import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'cancel-confirmation-modal',
  template: `
  <div class="modal-header">
    <button type="button"
            class="close"
            aria-hidden="true"
            (click)="onModalClick(false)">
      <span class="pficon pficon-close"></span>
    </button>
    <h4 class="modal-title">Warning!</h4>
  </div>
  <div class="modal-body">
    <div class="row">
      <div class="col-xs-12">
        <ng-content select="modal-title"></ng-content>
        <ng-content select="modal-content"></ng-content>
      </div>
    </div>
  </div>
  <div class="modal-footer">
    <button type="button"
            class="btn btn-danger"
            (click)="onModalClick(true)">
      Cancel
    </button>
    <button type="button"
            class="btn btn-default"
            (click)="onModalClick(false)">
      Continue
    </button>
  </div>
  `
})
export class CancelConfirmationModalComponent {
  @Output() cancel = new EventEmitter<boolean>();

  onModalClick(doCancel: boolean): void {
    this.cancel.emit(doCancel);
  }
}
