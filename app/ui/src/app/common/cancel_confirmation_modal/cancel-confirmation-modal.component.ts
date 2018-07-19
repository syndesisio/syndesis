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
    <h4 class="modal-title">{{ title }}</h4>
  </div>
  <div class="modal-body">
    <span aria-hidden="true" class="pficon pficon-warning-triangle-o"></span>
    <div>
      <ng-content></ng-content>
    </div>
  </div>
  <div class="modal-footer">
    <button type="button"
            class="btn btn-default"
            (click)="onModalClick(false)">
      {{ secondaryLabel }}
    </button>
    <button type="button"
            class="btn btn-primary"
            (click)="onModalClick(true)">
      {{ primaryLabel }}
    </button>
  </div>
  `
})
export class CancelConfirmationModalComponent {
  @Input() primaryLabel = 'Cancel';
  @Input() secondaryLabel = 'Continue';
  @Input() title = 'Warning!';
  @Output() cancel = new EventEmitter<boolean>();

  onModalClick(doCancel: boolean): void {
    this.cancel.emit(doCancel);
  }
}
