import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  OpenApiUploadSpecification,
  OpenApiValidationErrorMessage
} from '@syndesis/ui/common';

@Component({
  selector: 'api-provider-spec-step-upload',
  templateUrl: './step-upload.component.html',
})
export class StepUploadComponent {
  @Input() uploadSpecification: OpenApiUploadSpecification;
  @Input() errors: OpenApiValidationErrorMessage[];
  @Input() loading: boolean;
  @Output() onDone = new EventEmitter<void>();
  @Output() onUploadSpecificationChange = new EventEmitter<OpenApiUploadSpecification>();
}
