import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ApiProviderValidationResponse } from '@syndesis/ui/integration/api-provider/api-provider.models';

@Component({
  selector: 'api-provider-creation-step-validate',
  templateUrl: './step-validate.component.html',
})
export class StepValidateComponent {
  @Input() loading: boolean;
  @Input() validationResponse: ApiProviderValidationResponse;
  @Output() onDone = new EventEmitter<boolean>();
  @Output() onEdit = new EventEmitter<boolean>();
  @Output() onCancel = new EventEmitter<boolean>();

  hasErrors() {
    const errors = this.validationResponse.errors;

    return typeof(errors) !== 'undefined' && errors.length > 0;
  }
}
