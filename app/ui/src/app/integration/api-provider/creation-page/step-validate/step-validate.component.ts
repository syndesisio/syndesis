import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ActionReducerError } from '@syndesis/ui/platform';
import { ApiProviderValidationResponse } from '@syndesis/ui/integration/api-provider/api-provider.models';

@Component({
  selector: 'api-provider-creation-step-validate',
  templateUrl: './step-validate.component.html',
})
export class StepValidateComponent {
  @Input() loading: boolean;
  @Input() validationResponse: ApiProviderValidationResponse;
  @Input() creationError: ActionReducerError;
  @Output() onDone = new EventEmitter<boolean>();
  @Output() onEdit = new EventEmitter<boolean>();
  @Output() onCancel = new EventEmitter<boolean>();
}
