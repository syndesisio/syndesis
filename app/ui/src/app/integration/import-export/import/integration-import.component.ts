import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import {
  IntegrationOverviews,
  IntegrationSupportService
} from '@syndesis/ui/platform';
import { FileError, IntegrationImportsData } from './integration-import.models';
import { Observable } from 'rxjs/Observable';

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
export class IntegrationImportComponent implements OnInit {
  error: FileError;
  importedOverviews$: Observable<IntegrationOverviews>;
  importing = false;
  isDragAndDropImport: boolean;
  isMultipleImport: boolean;
  item = {} as FileItem;
  loading = true;
  response: IntegrationImportsData;
  showButtons = false;
  showReviewStep = false;
  uploader: FileUploader;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(private integrationSupportService: IntegrationSupportService, private router: Router) {
    // Do stuff here!
  }

  cancel() {
    this.redirectBack();
  }

  done(importedOverviews) {
    if (importedOverviews.length === 1 && importedOverviews[0].id && !this.isMultipleImport) {
      this.router.navigate(['/integrations', importedOverviews[0].id]);
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

  onDropFile(): void {
    this.isDragAndDropImport = true;
    this.isMultipleImport = this.checkIfMultiple();
  }

  onDropOverAndOut(event: Event): void {
    this.checkIfDragAndDrop(event);
  }

  onFileSelected(): void {
    this.isDragAndDropImport = false;
    this.isMultipleImport = this.checkIfMultiple();
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
      if (status === 200) {
        this.fetchImportedIntegrations(JSON.parse(response));
      }
    };
  }

  private checkIfDragAndDrop(isDragAndDropImport): void {
    this.isDragAndDropImport = !!isDragAndDropImport;
  }

  private checkIfMultiple(): boolean {
    return this.uploader.queue.length > 1;
  }

  private fetchImportedIntegrations(results) {
    this.importedOverviews$ = this.integrationSupportService.getOverviews().map(overviews => {
      return overviews.filter((overview) => {
        return results.find((result) => result.id === overview.id) !== -1;
      });
    });

    this.showButtons = true;
    this.showReviewStep = true;
  }

  private redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
