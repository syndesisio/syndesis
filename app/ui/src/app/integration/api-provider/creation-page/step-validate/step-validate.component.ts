import { Component, EventEmitter, Input, Output } from '@angular/core';
import { OpenApiValidationResponse } from '@syndesis/ui/common';
import { ActionReducerError } from '@syndesis/ui/platform';

@Component({
  selector: 'api-provider-creation-step-validate',
  templateUrl: './step-validate.component.html',
})
export class StepValidateComponent {
  @Input() loading: boolean;
  @Input() validationResponse: OpenApiValidationResponse;
  @Input() creationError: ActionReducerError;
  @Output() onDone = new EventEmitter<boolean>();
  @Output() onEdit = new EventEmitter<boolean>();
  @Output() onCancel = new EventEmitter<boolean>();
}
