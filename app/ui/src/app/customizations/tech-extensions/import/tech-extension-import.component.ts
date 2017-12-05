import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FileUploader,
  FileItem,
  FileLikeObject,
  FilterFunction,
  FileUploaderOptions,
  ParsedResponseHeaders
} from 'ng2-file-upload-base/src';
import { NotificationType } from 'patternfly-ng';
import { Extension } from '../../../model';
import { ExtensionStore } from '../../../store/extension/extension.store';
import { NotificationService } from 'app/common/ui-patternfly/notification-service';

interface FileError {
  level: string;
  message: string;
}

@Component({
  selector: 'syndesis-tech-extentions-import',
  templateUrl: 'tech-extension-import.component.html',
  styleUrls: ['tech-extension-import.component.scss']
})
export class TechExtensionImportComponent implements OnInit {

  uploader: FileUploader;
  response: Extension;
  error: FileError;
  importing = false;
  extensionId: string;
  extensionName: string;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(private extensionStore: ExtensionStore,
              private notificationService: NotificationService,
              private router: Router,
              private route: ActivatedRoute) { }

  getGenericError() {
    return {
        level: 'alert alert-danger',
        message: '<strong>This is not a valid file type.</strong> Try again and specify a .jar file'
      };
  }

  doImport() {
    this.importing = true;
    if (!this.response || !this.response.id) {
      // safety net
      this.response = undefined;
      return;
    }
    this.extensionStore.importExtension(this.response.id).toPromise().then( value => {
      this.notificationService.popNotification({
        type: NotificationType.SUCCESS,
        header: 'Imported!',
        message: 'Your technical extension has been imported'
      });
      const id = this.extensionId || this.response.id;
      this.router.navigate(['/customizations/tech-extensions', id], { relativeTo: this.route });
    }).catch((reason: any) => {
      this.error = {
        level: 'alert alert-danger',
        message: reason.userMsg || 'An unknown error has occurred'
      };
    });
  }

  ngOnInit() {
    this.extensionId = this.route.snapshot.paramMap.get('id');
    this.extensionName = this.route.snapshot.paramMap.get('name');
    if (!this.extensionName) {
      // safety net
      this.extensionId = undefined;
    }
    const uploadUrl = this.extensionStore.getUploadUrl(this.extensionId);
    this.uploader = new FileUploader({
      url: uploadUrl,
      disableMultipart: false,
      autoUpload: true,
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
    this.uploader.onWhenAddingFileFailed = (item: FileLikeObject, filter: any, options: any): any => {
      this.error = this.getGenericError();
      this.fileSelect.nativeElement['value'] = '';
      this.uploader.clearQueue();
    };
    this.uploader.onCompleteItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
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
        this.response = <Extension> resp;
        return;
      }
      this.error = {
        level: 'alert alert-danger',
        message: resp.userMsg || 'An unknown error has occurred'
      };
    };
  }
}
