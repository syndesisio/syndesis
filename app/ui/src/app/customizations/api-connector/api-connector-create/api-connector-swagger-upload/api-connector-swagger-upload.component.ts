import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  OpenApiUploadSpecification,
  OpenApiValidationErrorMessage
} from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-api-connector-swagger-upload',
  templateUrl: './api-connector-swagger-upload.component.html',
  styleUrls: ['./api-connector-swagger-upload.component.scss']
})
export class ApiConnectorSwaggerUploadComponent {

  @Input() errors: OpenApiValidationErrorMessage[];
  @Input() loading: boolean;
  @Input() uploadSpecification: OpenApiUploadSpecification;

  @Output() onCancel = new EventEmitter<void>();
  @Output() onDone = new EventEmitter<void>();
  @Output() onUploadSpecificationChange = new EventEmitter<OpenApiUploadSpecification>();

}
