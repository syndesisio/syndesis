import { Component, Output, EventEmitter, OnInit, Input, ElementRef, ViewChild } from '@angular/core';

import {
  ApiConnectorState,
  CustomConnectorRequest,
  ApiConnectorValidationError
} from '@syndesis/ui/customizations/api-connector';

import { I18NService } from '@syndesis/ui/platform';

import {
  FileLikeObject,
  FileUploader,
  FileUploaderOptions
} from '@syndesis/ui/vendor';

@Component({
  selector: 'syndesis-api-connector-swagger-upload',
  templateUrl: './api-connector-swagger-upload.component.html',
  styleUrls: ['./api-connector-swagger-upload.component.scss']
})
export class ApiConnectorSwaggerUploadComponent implements OnInit {
  @Input() apiConnectorState: ApiConnectorState;
  @Output() request = new EventEmitter<CustomConnectorRequest>();
  @ViewChild('fileSelect') fileSelect: ElementRef;

  fileToUpload: File;
  hasBaseDropZoneOver: boolean;
  invalidFileMsg: string;
  swaggerFileUrl: string;
  uploader: FileUploader;

  constructor( private i18NService: I18NService ) {
    // nothing to do
  }

  ngOnInit() {
    this.uploader = new FileUploader(
      {
        allowedMimeType: [ 'application/json' ],
        filters: [
          {
            name: 'filename filter',
            fn: ( item: FileLikeObject, options: FileUploaderOptions ) => {
              return item.name.endsWith( '.json' );
            }
          }
        ]
      }
    );

    this.uploader.onAfterAddingFile = () => {
      // successfully added file so clear out failed message
      this.invalidFileMsg = null;

      // since more than one file may have been dropped, clear out all but last one
      if ( this.uploader.queue.length > 1 ) {
        this.uploader.queue.splice( 0, 1 );
      }

      // pop off file from queue to set file and clear queue
      this.fileToUpload = this.uploader.queue.pop()._file;
    };

    this.uploader.onWhenAddingFileFailed = (
      file: FileLikeObject
    ): any => {
      // occurs when not a *.json file
      this.invalidFileMsg = this.i18NService.localize( 'customizations.api-client-connectors.api-upload-invalid-file',
                                                       [ file.name ] );
      this.fileToUpload = null;
      this.fileSelect.nativeElement.value = '';
      this.uploader.clearQueue();
    };
  }

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

  onFileDrop(e) {
    // clear out text next to 'Choose File' button
    this.fileSelect.nativeElement.value = '';
  }

  onFileOver(e) {
    this.hasBaseDropZoneOver = e;
  }

  onSubmit({ valid }, attachFile: boolean): void {
    if ((this.swaggerFileUrl && valid) || this.fileToUpload ) {
      const validateSwaggerRequest = {
        connectorTemplateId: 'swagger-connector-template',
        configuredProperties: {
          specification: this.swaggerFileUrl
        },
        specificationFile: attachFile && this.fileToUpload
      };

      this.request.next(validateSwaggerRequest);
    }
  }
}
