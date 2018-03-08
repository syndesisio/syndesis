import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';

import { NotificationType } from 'patternfly-ng';
import { NotificationService } from '@syndesis/ui/common';
import { Integration, Integrations, IntegrationOverviews, IntegrationSupportService } from '@syndesis/ui/platform';
import { FileError, IntegrationImportsData } from './integration-import.models';
import { Observable } from 'rxjs/Observable';

import {
  FileItem,
  FileLikeObject,
  FilterFunction,
  FileUploader,
  FileUploaderOptions,
  ParsedResponseHeaders
} from 'ng2-file-upload';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html',
  styleUrls: ['./integration-import.component.scss']
})
export class IntegrationImportComponent implements OnInit {
  error: FileError;
  importing = false;
  uploader: FileUploader;
  hasBaseDropZoneOver: boolean;
  response: IntegrationImportsData;
  integrationId: string;
  integrations$: Observable<IntegrationOverviews>;
  integrationName: string;
  integrationImports$: Observable<IntegrationOverviews>;
  integrationUpdate = false;
  item = { } as FileItem;
  loading = true;
  review = false;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(
    public notificationService: NotificationService,
    private integrationSupportService: IntegrationSupportService,
  ) {}

  getFileTypeError() {
    return {
      level: 'alert alert-danger',
      message: '<strong>This is not a valid file type.</strong> Try again and specify a .zip file.'
    };
  }

  onFileOver(e) {
    this.hasBaseDropZoneOver = e;
  }

  ngOnInit() {
    this.uploader = new FileUploader({
      url: this.integrationSupportService.importIntegrationURL(),
      disableMultipart: true,
      autoUpload: true,
      //removeAfterUpload: true,
      filters: [
        {
          name: 'filename filter',
          fn: (item: FileLikeObject, options: FileUploaderOptions) => {
            return item.name.endsWith('.zip');
          }
        }
      ]
    });

    this.uploader.onAfterAddingFile = (item: FileItem) => {
      console.log('File has been added: ' + item);
    };

    this.uploader.onWhenAddingFileFailed = (item: FileLikeObject, filter: any, options: any): any => {
      this.error = this.getFileTypeError();
      this.fileSelect.nativeElement['value'] = '';
      this.uploader.clearQueue();
    };

    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number
    ) => {
      console.log('File has been uploaded: ' + item);
      console.log('Response: ' + JSON.stringify(response));

      if (status === 200) {
        this.review = true;
        this.integrationImports$ = JSON.parse(response);
      }
    };
  }
}
