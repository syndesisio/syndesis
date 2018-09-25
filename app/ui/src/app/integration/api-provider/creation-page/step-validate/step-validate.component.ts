import { Component, EventEmitter, Input, Output } from '@angular/core';
import { OpenApiValidationResponse } from '@syndesis/ui/common';

@Component({
  selector: 'api-provider-creation-step-validate',
  templateUrl: './step-validate.component.html',
})
export class StepValidateComponent {
  @Input() loading: boolean;
  @Input() validationResponse: OpenApiValidationResponse;
  @Output() onDone = new EventEmitter<boolean>();
  @Output() onEdit = new EventEmitter<boolean>();
  @Output() onBack = new EventEmitter<boolean>();
}
