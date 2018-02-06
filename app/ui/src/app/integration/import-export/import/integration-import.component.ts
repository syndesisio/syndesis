import {Component, EventEmitter, Input, Output } from '@angular/core';
import { IntegrationStore } from '@syndesis/ui/store';
import { Integrations, IntegrationSupportService } from '@syndesis/ui/platform';
import { NotificationService } from '@syndesis/ui/common';
import { Extension } from '@syndesis/ui/platform';
import {
  IntegrationImportValidationError,
  IntegrationUploadValidationError,
  IntegrationImportRequest,
  IntegrationImportState
} from './integration-import.models';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html',
  styleUrls: ['./integration-import.component.scss']
})
export class IntegrationImportComponent {
  @Input() integrationImportState: IntegrationImportState;
  @Output() request = new EventEmitter<IntegrationImportRequest>();
  fileUrl: string;
  fileList: FileList;

  get validationError(): IntegrationUploadValidationError {
    if (this.integrationImportState &&
      this.integrationImportState.uploadRequest &&
      this.integrationImportState.uploadRequest.errors &&
      this.integrationImportState.uploadRequest.errors.length > 0) {
      return this.integrationImportState.uploadRequest.errors[0];
    }
  }

  get processingError(): string {
    if (this.integrationImportState &&
      this.integrationImportState.hasErrors &&
      this.integrationImportState.errors.length > 0) {
      return this.integrationImportState.errors[0].message;
    }
  }

  onFile(event): void {
    if (event.target && event.target.files) {
      this.fileList = event.target.files;
    } else {
      this.fileList = null;
    }
  }

  onSubmit({ valid }, attachFile: boolean): void {
    if ((this.fileUrl && valid) || this.fileList) {
      const validateRequest = {
        integrationTemplateId: 'integration-import-template',
        configuredProperties: {
          specification: this.fileUrl
        },
        specificationFile: attachFile && this.fileList && this.fileList[0]
      };

      this.request.next(validateRequest);
    }
  }
}
