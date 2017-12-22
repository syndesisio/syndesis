import { Component, Input } from '@angular/core';

@Component({
  selector: 'syndesis-button',
  template: `
    <button [attr.type]="type" class="submit syndesis-button syn-form__submit btn btn-primary"
      [ngClass]="{ 'syndesis-button--loading': loading, 'syndesis-button--disabled': disabled }"
      [attr.disabled]="(disabled || loading) ? 'disabled' : null">
      <i class="spinner spinner-sm syndesis-button__spinner" *ngIf="loading"></i>
      <span class="syndesis-button__label">
        <ng-content></ng-content>
      </span>
    </button>
  `,
  styles: [`
    .syndesis-button { display: flex; justify-content: center; }
    .syndesis-button__spinner { display: block; margin: 0; }
    .syndesis-button__spinner + .syndesis-button__label { padding-left: 1em; }
  `]
})
export class ButtonComponent {
  @Input() type = 'button';
  @Input() disabled: boolean;
  @Input() loading: boolean;
}
