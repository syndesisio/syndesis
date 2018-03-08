import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';

import {
  Integration, Integrations, IntegrationOverviews, IntegrationSupportService,
  IntegrationOverview, Extension
} from '@syndesis/ui/platform';
import {FileError, IntegrationImportsData} from './integration-import.models';
import {Observable} from 'rxjs/Observable';

import {
  FileItem,
  FileLikeObject,
  FileUploader,
  FileUploaderOptions
} from 'ng2-file-upload';
import {Subscription} from "rxjs/Subscription";

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
  integrations: Array<IntegrationOverview>;
  integrationOverviews$: Observable<IntegrationOverviews>;
  integrationImports$: Observable<IntegrationOverviews>;
  item = {} as FileItem;
  review = false;

  private integrationOverviewsSubscription: Subscription;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(private integrationSupportService: IntegrationSupportService,) {
  }

  cancel() {
    console.log('Cancel');
  }

  done() {
    console.log('Done');
  }

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
    this.integrationOverviews$ = this.integrationSupportService.watchOverviews();
    this.integrationOverviewsSubscription = this.integrationOverviews$.subscribe(integrations => {
      this.integrations = integrations;
    });

    this.uploader = new FileUploader({
      url: this.integrationSupportService.importIntegrationURL(),
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

    this.uploader.onWhenAddingFileFailed = (item: FileLikeObject, filter: any, options: any): any => {
      this.error = this.getFileTypeError();
      this.fileSelect.nativeElement['value'] = '';
      this.uploader.clearQueue();
    };

    this.uploader.onCompleteItem = (item: FileItem,
                                    response: string,
                                    status: number) => {
      console.log('File has been uploaded: ' + item);
      console.log('File item: ' + JSON.stringify(item.file));
      console.log('Form data: ' + JSON.stringify(item.formData));
      console.log('Response: ' + JSON.stringify(response));
      //this.;

      if (status === 200) {
        this.review = true;
        this.integrationImports$ = JSON.parse(response);
        this.item = item;
      }
    };
  }

  ngOnDestroy() {
    if (this.integrationOverviewsSubscription) {
      this.integrationOverviewsSubscription.unsubscribe();
    }
  }
}
