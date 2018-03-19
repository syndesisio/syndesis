import { Component, ElementRef, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import {
  Integration,
  Integrations,
  IntegrationOverview,
  IntegrationOverviews,
  IntegrationSupportService
} from '@syndesis/ui/platform';
import { FileError, IntegrationImportsData } from './integration-import.models';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import {
  FileItem,
  FileLikeObject,
  FileUploader,
  FileUploaderOptions
} from 'ng2-file-upload';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html',
  styleUrls: ['./integration-import.component.scss']
})
export class IntegrationImportComponent implements OnInit, OnDestroy {
  error: FileError;
  importing = false;
  uploader: FileUploader;
  isDragAndDropImport: boolean;
  isMultipleImport: boolean;
  response: IntegrationImportsData;
  integrations: Array<IntegrationOverview>;
  integrationOverviews$: Observable<IntegrationOverviews>;
  integrationImports$: Observable<IntegrationOverviews>;
  item = {} as FileItem;
  review = false;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  private integrationOverviewsSubscription: Subscription;
  constructor(private integrationSupportService: IntegrationSupportService, private router: Router) {
  }

  cancel() {
    this.redirectBack();
  }

  done(integrationImports) {
    if (integrationImports.length === 1 && integrationImports[0].id) {
      this.router.navigate(['/integrations', integrationImports[0].id]);
    } else {
      this.redirectBack();
    }
  }

  getFileTypeError() {
    return {
      level: 'alert alert-danger',
      message: '<strong>This is not a valid file type.</strong> Try again and specify a .zip file.'
    };
  }

  onFileSelected(event: Event): void {
    this.isDragAndDropImport = false;
    this.isMultipleImport = this.checkIfMultiple();
    console.log('onFileSelected: ' + JSON.stringify(event));
  }

  onDropOverAndOut(event: Event): void {
    console.log('onDropOverAndOut(event): ' + JSON.stringify(event));
    this.checkIfDragAndDrop(event);
  }

  onDropFile(event: Event): void {
    console.log('onDropFile: ' + JSON.stringify(event));
    this.isMultipleImport = this.checkIfMultiple();
    this.isDragAndDropImport = true;
  }

  ngOnInit() {
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
      console.log('onCompleteItem response: ' + response);
      console.log('onCompleteItem response.length: ' + response.length);
      if (status === 200) {
        console.log('Review: ' + this.review);
        this.review = !this.checkIfMultiple();
        console.log('Review: ' + this.review);
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

  private checkIfDragAndDrop(isDragAndDropImport): void {
    this.isDragAndDropImport = !!isDragAndDropImport;
  }

  private checkIfMultiple(): boolean {
    console.log('this.upload.queue.length: ' + this.uploader.queue.length);
    return this.uploader.queue.length >1;
  }

  private redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
