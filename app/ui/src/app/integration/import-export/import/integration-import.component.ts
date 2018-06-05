import { map } from 'rxjs/operators';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import {
  IntegrationOverviews,
  IntegrationSupportService
} from '@syndesis/ui/platform';
import { FileError, IntegrationImportsData } from './integration-import.models';
import { Observable } from 'rxjs';

import {
  FileItem,
  FileLikeObject,
  FileUploader,
  FileUploaderOptions
} from 'ng2-file-upload';
import { IntegrationStore } from '@syndesis/ui/store';
import { environment } from '../../../../environments/environment';
import { HttpXsrfTokenExtractor } from '@angular/common/http';
import { NotificationType } from 'patternfly-ng';
import { NotificationService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html',
  styleUrls: ['./integration-import.component.scss']
})
export class IntegrationImportComponent implements OnInit {
  error: FileError;
  importedOverviews$: Observable<IntegrationOverviews>;
  importing = false;
  isMultipleImport: boolean;
  item = {} as FileItem;
  loading = true;
  response: IntegrationImportsData;
  showButtons = false;
  showReviewStep = false;
  uploader: FileUploader;
  hasBaseDropZoneOver: boolean;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(
    private integrationSupportService: IntegrationSupportService,
    private integrationStore: IntegrationStore,
    private router: Router,
    public notificationService: NotificationService,
    private tokenExtractor: HttpXsrfTokenExtractor
  ) {
    // Do stuff here!
  }

  cancel() {
    this.redirectBack();
  }

  done(importedOverviews) {
    if (
      importedOverviews.length === 1 &&
      importedOverviews[0].id &&
      !this.isMultipleImport
    ) {
      this.router.navigate(['/integrations', importedOverviews[0].id]);
    } else {
      this.redirectBack();
    }
  }

  getFileTypeError() {
    return {
      level: 'alert alert-danger',
      message:
        '<strong>This is not a valid file type.</strong> Try again and specify a .zip file.'
    };
  }

  onDropFile(): void {
    this.isMultipleImport = this.checkIfMultiple();
  }

  onFileSelected(): void {
    this.isMultipleImport = this.checkIfMultiple();
  }

  onFileOver(e) {
    this.hasBaseDropZoneOver = e;
  }

  ngOnInit() {
    this.uploader = new FileUploader({
      url: this.integrationSupportService.importIntegrationURL(),
      headers: [
        {
          name: environment.xsrf.headerName,
          value:
            this.tokenExtractor.getToken() || environment.xsrf.defaultTokenValue
        }
      ],
      disableMultipart: true,
      autoUpload: true,
      filters: [
        {
          name: 'filename filter',
          fn: (item: FileLikeObject, options: FileUploaderOptions) => {
            return item.name.endsWith('.zip');
          }
        }
      ]
    });

    this.uploader.onWhenAddingFileFailed = (
      item: FileLikeObject,
      filter: any,
      options: any
    ): any => {
      this.notificationService.popNotification({
        type: NotificationType.DANGER,
        header: 'Import Failed',
        message: 'There was an issue importing your integration.',
        isPersistent: false
      });

      this.error = this.getFileTypeError();
      this.fileSelect.nativeElement['value'] = '';
      this.uploader.clearQueue();
    };

    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number
    ) => {
      if (status === 200) {
        this.fetchImportedIntegrations(JSON.parse(response));

        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Successfully Imported',
          message: 'Your integration has been imported',
          isPersistent: false
        });
      }
    };
  }

  private checkIfMultiple(): boolean {
    return this.uploader.queue.length > 1;
  }

  private fetchImportedIntegrations(results) {
    this.importedOverviews$ = this.integrationStore.list.pipe(
      map(integrations => {
        return integrations.filter(integration => {
          return results.find(result => result.id === integration.id) !== -1;
        });
      })
    );

    this.showButtons = true;
    this.showReviewStep = true;
  }

  private redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
