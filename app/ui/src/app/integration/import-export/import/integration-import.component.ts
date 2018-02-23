import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { NotificationType } from 'patternfly-ng';
import { NotificationService } from '@syndesis/ui/common';
import { IntegrationSupportService } from '@syndesis/ui/platform';

import {
  FileUploader,
  FileItem,
  ParsedResponseHeaders
} from 'ng2-file-upload';

import {
  IntegrationImportRequest,
  IntegrationImportState,
  IntegrationImportValidationError
} from '@syndesis/ui/integration/import-export/import';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html',
  styleUrls: ['./integration-import.component.scss']
})
export class IntegrationImportComponent implements OnInit {
  public uploader: FileUploader;
  public hasBaseDropZoneOver = false;

  constructor(
    public notificationService: NotificationService,
    private integrationSupportService: IntegrationSupportService,
  ) {}

  public fileOverBase(e) {
    this.hasBaseDropZoneOver = e;
  }

  ngOnInit() {
    this.uploader = new FileUploader({
      url: this.integrationSupportService.importIntegrationURL(),
      disableMultipart: true,
      autoUpload: true
    });

    /*
    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number,
      headers: ParsedResponseHeaders
    ) => {
      if (status === 200) {
        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Imported Integrations',
          message: 'Your integrations have been imported',
          isPersistent: true,
        });
      } else if (status === 400) {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Import Failed!',
          message: JSON.parse(response).userMsg,
          isPersistent: true,
        });
      } else {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Import Failed!',
          message: 'Your integration could not be imported.'
        });
      }
    };
    */
  }

  /*
  @Input() integrationImportState: IntegrationImportState;
  @Output() request = new EventEmitter<IntegrationImportRequest>();
  integrationImportFileList: FileList;

  get validationError(): IntegrationImportValidationError {
    if (this.integrationImportState &&
      this.integrationImportState.createRequest &&
      this.integrationImportState.createRequest.errors &&
      this.integrationImportState.createRequest.errors.length > 0) {
      return this.integrationImportState.createRequest.errors[0];
    }
  }

  onSelectFile(event): void {
    if (event.target && event.target.files) {
      this.integrationImportFileList = event.target.files;
    } else {
      this.integrationImportFileList = null;
    }
  }

  onDone({ valid }, attachFile: boolean): void {
    if (this.integrationImportFileList) {
      const validateImportRequest = {
        file: attachFile && this.integrationImportFileList && this.integrationImportFileList[0],
        integrationImportTemplateId: 'integration-import-template'
      };

      this.request.next(validateImportRequest);
    }
  }
  */
}
