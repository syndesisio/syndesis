import { Component, Output, EventEmitter, OnInit, Input } from '@angular/core';

import {
  ApiConnectorState,
  CustomConnectorRequest,
  ApiConnectorValidationError
} from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-swagger-upload',
  templateUrl: './api-connector-swagger-upload.component.html',
  styleUrls: ['./api-connector-swagger-upload.component.scss']
})
export class ApiConnectorSwaggerUploadComponent {
  @Input() apiConnectorState: ApiConnectorState;
  @Output() request = new EventEmitter<CustomConnectorRequest>();
  swaggerFileUrl: string;
  swaggerFileList: FileList;

  get validationError(): ApiConnectorValidationError {
    if (
      this.apiConnectorState &&
      this.apiConnectorState.createRequest &&
      this.apiConnectorState.createRequest.errors &&
      this.apiConnectorState.createRequest.errors.length > 0
    ) {
      return this.apiConnectorState.createRequest.errors[0];
    }
  }

  get processingError(): string {
    if (
      this.apiConnectorState &&
      this.apiConnectorState.hasErrors &&
      this.apiConnectorState.errors.length > 0
    ) {
      return this.apiConnectorState.errors[0].message;
    }
  }

  onFile(event): void {
    if (event.target && event.target.files) {
      this.swaggerFileList = event.target.files;
    } else {
      this.swaggerFileList = null;
    }
  }

  onSubmit({ valid }, attachFile: boolean): void {
    if ((this.swaggerFileUrl && valid) || this.swaggerFileList) {
      const validateSwaggerRequest = {
        connectorTemplateId: 'swagger-connector-template',
        configuredProperties: {
          specification: this.swaggerFileUrl
        },
        specificationFile:
          attachFile && this.swaggerFileList && this.swaggerFileList[0]
      };

      this.request.next(validateSwaggerRequest);
    }
  }
}
