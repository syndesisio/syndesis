import { Component } from '@angular/core';

@Component({
  selector: 'syndesis-validation-error',
  styleUrls: ['./validation-error.component.scss'],
  template: `
    <p class="syn-validation-error">
      <i class="syn-validation-error__icon fa fa-times"></i>
      <strong>
        <ng-content></ng-content>
      </strong>
    </p>
  `
})
export class ValidationErrorComponent {}
