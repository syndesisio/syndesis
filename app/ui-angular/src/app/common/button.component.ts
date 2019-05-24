import { Component, Input } from '@angular/core';

@Component({
  selector: 'syndesis-button',
  template: `
    <button
      [attr.type]="type"
      class="submit syndesis-button syn-form__submit btn"
      [class.btn-primary]="theme == 'primary'"
      [class.btn-default]="theme == 'default'"
      (click)="(click)"
      [ngClass]="{
        'syndesis-button--loading': loading,
        'syndesis-button--disabled': disabled
      }"
      [disabled]="disabled || loading ? true : false"
    >
      <i
        class="spinner spinner-sm syndesis-button__spinner"
        *ngIf="loading"
      ></i>
      <span class="syndesis-button__label"> <ng-content></ng-content> </span>
    </button>
  `,
  styles: [
    `
      :host-context(.is-margin-reset) .submit {
        margin: 0;
      }
      ::ng-deep syndesis-button + syndesis-button {
        margin-left: 5px;
      }
      .syndesis-button {
        display: inline-flex;
        justify-content: center;
      }
      .syndesis-button__spinner {
        display: block;
        margin: 0;
      }
      .syndesis-button__spinner + .syndesis-button__label {
        padding-left: 1em;
      }
    `,
  ],
})
export class ButtonComponent {
  @Input() type: 'button' | 'submit' = 'button';
  @Input() theme: 'primary' | 'default' = 'primary';
  @Input() disabled: boolean;
  @Input() loading: boolean;
  @Input() click = () => {
    // Default click hander, noop
  }
}
