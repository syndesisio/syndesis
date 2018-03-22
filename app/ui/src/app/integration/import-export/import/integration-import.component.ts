import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import {
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
export class IntegrationImportComponent implements OnInit {
  error: FileError;
  fetchedIntegrations: Array<IntegrationOverview>;
  filteredIntegrations: Array<IntegrationOverview>;
  importing = false;
  integrationOverviews$: Observable<IntegrationOverviews>;
  isDragAndDropImport: boolean;
  isMultipleImport: boolean;
  item = {} as FileItem;
  loading = true;
  response: IntegrationImportsData;
  showButtons = false;
  showReviewStep = false;
  uploader: FileUploader;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  private integrationOverviewsSubscription: Subscription;
  constructor(private integrationSupportService: IntegrationSupportService, private router: Router) {
    this.filteredIntegrations = [];
  }

  cancel() {
    this.redirectBack();
  }

  done(filteredIntegrations) {
    if (filteredIntegrations.length === 1 && filteredIntegrations[0].id && !this.isMultipleImport) {
      this.router.navigate(['/integrations', filteredIntegrations[0].id]);
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
        this.fetchIntegrationOverview(JSON.parse(response));
      }
    };
  }

  private checkIfDragAndDrop(isDragAndDropImport): void {
    this.isDragAndDropImport = !!isDragAndDropImport;
  }

  private checkIfMultiple(): boolean {
    return this.uploader.queue.length > 1;
  }

  private fetchIntegrationOverview(results) {
    this.integrationOverviews$ = this.integrationSupportService.getOverviews();

    this.integrationOverviewsSubscription = this.integrationOverviews$.subscribe(integrations => {
      this.fetchedIntegrations = integrations;

      this.filteredIntegrations = this.filterIntegrations(results, this.fetchedIntegrations);
      //this.filteredIntegrations = this.filterIntegrations(results, integrations);

      this.showButtons = true;

      //this.showReviewStep = !this.checkIfMultiple();
      this.showReviewStep = true;
    });
  }

  private filterIntegrations(results, fetchedIntegrations) {
    const tempArray = [];

    // First iterate over list of results, then iterate over integration overviews fetched
    // Finally, push to a temporary array of integration overviews if IDs match

    (results || []).forEach(result => {
      (fetchedIntegrations || []).forEach(integration => {
        if (result.id === integration.id) {
          tempArray.push(integration);
        }
      });
    });

    return tempArray;
  }

  private redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
