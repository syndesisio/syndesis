import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import {
  FileUploader,
  FileItem,
  FileLikeObject,
  FileUploaderOptions,
  ParsedResponseHeaders
} from 'ng2-file-upload';
import { NotificationType } from 'patternfly-ng';

import { Extension, I18NService } from '@syndesis/ui/platform';
import { NotificationService } from '@syndesis/ui/common';
import { ExtensionStore } from '@syndesis/ui/store/extension/extension.store';

import { environment } from 'environments/environment';
import { HttpXsrfTokenExtractor } from '@angular/common/http';

interface FileError {
  level: string;
  message: string;
}

@Component({
  selector: 'syndesis-tech-extentions-import',
  templateUrl: 'extension-import.component.html',
  styleUrls: [
    '../extension-common.scss',
    'extension-import.component.scss'
  ]
})
export class ExtensionImportComponent implements OnInit {
  uploader: FileUploader;
  response: Extension;
  error: FileError;
  importing = false;
  extensionId: string;
  extensionName: string;
  extension$: Observable<Extension>;
  extensionUpdate = false;
  hasBaseDropZoneOver: boolean;
  item = {} as FileItem;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(
    private extensionStore: ExtensionStore,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute,
    private tokenExtractor: HttpXsrfTokenExtractor,
    private i18NService: I18NService
  ) {
    this.extension$ = this.extensionStore.resource;
  }

  getGenericError() {
    return {
      level: 'alert alert-danger',
      message: this.i18NService.localize( 'customizations.extensions.import-extension-generic-error' )
    };
  }

  onFileOver(e) {
    this.hasBaseDropZoneOver = e;
  }

  doImport() {
    this.importing = true;
    if (!this.response || !this.response.id) {
      // safety net
      this.response = undefined;
      return;
    }
    this.extensionStore
      .importExtension(this.response.id)
      .toPromise()
      .then(value => {
        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: this.i18NService.localize( 'customizations.extensions.import-extension-success-header' ),
          message: this.i18NService.localize( 'customizations.extensions.import-extension-success-message' )
        });
        this.router.navigate(['/customizations/extensions'], {
          relativeTo: this.route
        });
      })
      .catch((reason: any) => {
        this.error = {
          level: 'alert alert-danger',
          message: reason.userMsg
                   || this.i18NService.localize( 'customizations.extensions.import-extension-unknown-error-message' )
        };
      });
  }

  ngOnInit() {
    this.extensionId = this.route.snapshot.paramMap.get('id');
    if (this.extensionId) {
      this.extensionUpdate = true;
      this.extensionStore.load(this.extensionId);
    }
    const uploadUrl = this.extensionStore.getUploadUrl(this.extensionId);
    this.uploader = new FileUploader({
      url: uploadUrl,
      headers: [
        {
          name: environment.xsrf.headerName,
          value:
            this.tokenExtractor.getToken() || environment.xsrf.defaultTokenValue
        }
      ],
      disableMultipart: false,
      autoUpload: true,
      removeAfterUpload: true,
      filters: [
        {
          name: 'filename filter',
          fn: (item: FileLikeObject, options: FileUploaderOptions) => {
            if (!item.name.endsWith('.jar')) {
              return false;
            }
            return true;
          }
        }
      ]
    });
    this.uploader.onWhenAddingFileFailed = (
      item: FileLikeObject,
      filter: any,
      options: any
    ): any => {
      this.error = this.getGenericError();
      this.fileSelect.nativeElement['value'] = '';
      this.uploader.clearQueue();
    };
    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number,
      headers: ParsedResponseHeaders
    ) => {
      this.item = item;
      this.error = undefined;
      this.response = undefined;
      let resp: any = {};
      try {
        resp = JSON.parse(response);
      } catch (err) {
        this.error = this.getGenericError();
        return;
      }
      if (status === 200) {
        this.response = <Extension>resp;
        return;
      }
      this.error = {
        level: 'alert alert-danger',
        message: resp.userMsg
                 || this.i18NService.localize( 'customizations.extensions.import-extension-unknown-error-message' )
      };
    };
  }
}
