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
  integrationOverview$: Observable<IntegrationOverview>;
  integrationOverviews$: Observable<IntegrationOverviews>;
  integrationImports$: Observable<IntegrationOverviews>;
  item = {} as FileItem;
  loading = true;
  showReviewStep = false;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  private integrationOverviewSubscription: Subscription;
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
    this.integrationOverviews$ = this.integrationSupportService.watchOverviews();

    this.integrationOverviewsSubscription = this.integrationOverviews$.subscribe(integrations => {
      this.integrations = integrations;
      this.loading = false;
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
      if (status === 200) {
        this.showReviewStep = !this.checkIfMultiple();
        this.integrationImports$ = JSON.parse(response);
        //this.item = item;
        //console.log('integrationImports$: ' + JSON.stringify(this.integrationImports$));
        //[{"action":"updated","kind":"integration","id":"-L775DOUc8-A7n1KyeMq"}]
        //console.log('item: ' + this.item);
        //this.fetchIntegrationOverview(response);

        // NOTE: We should not have to parse the JSON here,
        // investigate with ng2-file-upload
        //this.integrations = JSON.parse(response);

        //console.log('this.integrations: ' + JSON.stringify(this.integrations));

        this.fetchIntegrationOverview(this.integrationImports$);

        /*
        if (this.showReviewStep) {
          console.log('this.showReviewStep true');
          //this.fetchIntegrationOverview(this.integrations[0].id);
          this.fetchIntegrationOverview(this.integrationImports$);
        } else {
          console.log('this.showReviewStep false');
          //this.fetchIntegrationOverview(this.integrations);
          //this.fetchIntegrationOverview(response);
        }
        */

        //(this.showReviewStep ? this.fetchIntegrationOverview(this.integrations[0].id) : this.fetchIntegrationOverview(this.integrations));
        //
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
    return this.uploader.queue.length >1;
  }

  private fetchIntegrationOverview(results) {
    if (this.checkIfMultiple() === false) {
      // Use generics to ensure the results value is just a string ID
      this.integrationOverview$ = this.integrationSupportService.getOverview(results[0].id);
      console.log('this.integrationOverview$: ' + JSON.stringify(this.integrationOverview$));
      this.integrationOverviewSubscription = this.integrationOverview$.subscribe(integration => {
        this.integrations.push(integration);
      });
    } else if (results && results.length) {
      // Use generics to ensure the results value is an array of integrations
      console.log('Multiple integrations, fetching..');
      //(this.integrations || []).forEach(integration => {});
      (results || []).forEach(integration => {
        console.log('each integration: ' + JSON.stringify(integration));
      });
      /*
      this.integrationOverviews$ = this.integrationSupportService.getOverviews();
      this.integrationOverviewsSubscription = this.integrationOverviews$.subscribe(integrations => {
        console.log('integrations returned value: ' + JSON.stringify(integrations));
        this.integrations = integrations;
        console.log('this.integrations: ' + JSON.stringify(this.integrations));
      });
      */
    } else {
      console.log('Nothing happening here..');
    }
  }

  private redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
