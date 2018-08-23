import { Component, ElementRef, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';

import {
  IntegrationSupportService,
  Integrations,
  Connections,
  StringMap,
  MessageLevel,
  I18NService
} from '@syndesis/ui/platform';
import { Subscription, Observable, combineLatest, BehaviorSubject } from 'rxjs';

import {
  FileItem,
  FileLikeObject,
  FileUploader,
  FileUploaderOptions
} from '@syndesis/ui/vendor';

import { IntegrationStore, ConnectionStore } from '@syndesis/ui/store';
import { environment } from 'environments/environment';
import { HttpXsrfTokenExtractor } from '@angular/common/http';

@Component({
  selector: 'syndesis-integration-import-page-component',
  templateUrl: './integration-import-page.component.html',
  styleUrls: ['./integration-import-page.component.scss']
})
export class IntegrationImportPageComponent implements OnInit, OnDestroy {
  _showReviewStep = false;
  uploader: FileUploader;
  hasBaseDropZoneOver: boolean;
  responses: StringMap<any>;
  subscription: Subscription;
  integrations$: Observable<Integrations>;
  connections$: Observable<Connections>;
  responses$ = new BehaviorSubject<StringMap<any>>({ initialize$: true });
  errors$ = new BehaviorSubject<Array<any>>([]);
  importedIntegrations$ = new BehaviorSubject<Integrations>([]);
  importedConnections$ = new BehaviorSubject<Connections>([]);

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(
    private integrationSupportService: IntegrationSupportService,
    private integrationStore: IntegrationStore,
    private connectionStore: ConnectionStore,
    private router: Router,
    private tokenExtractor: HttpXsrfTokenExtractor,
    private i18NService: I18NService
  ) {
    this.integrations$ = this.integrationStore.list;
    this.connections$ = this.connectionStore.list;
  }

  cancel() {
    this.redirectBack();
  }

  done() {
    this.redirectBack();
  }

  get showReviewStep() {
    return this._showReviewStep;
  }

  getErrorMessage(errorObject) {
    let serverResponse = errorObject.response;
    try {
      serverResponse = JSON.parse(serverResponse)['developerMsg'] || errorObject.response;
    } catch (err) {
      // ignore, we'll just show the raw response
    }
    const message = this.i18NService.localize('integrations.import-export.upload-error', [errorObject.fileName, serverResponse]);
    return {
      level: MessageLevel.ERROR,
      message
    };
  }

  initialize(): any {
    this._showReviewStep = false;
    this.importedIntegrations$.next([]);
    this.importedConnections$.next([]);
    this.errors$.next([]);
    this.responses = {};
    this.responses$.next({});
  }

  onFileOver(e) {
    this.hasBaseDropZoneOver = e;
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  ngOnInit() {
    this.subscription = combineLatest<Integrations, Connections, StringMap<any>>(
      this.integrations$,
      this.connections$,
      this.responses$
    ).subscribe(allTheThings => {
      const integrations = allTheThings[0];
      const connections = allTheThings[1];
      const responses = allTheThings[2];
      if (responses.initialize$) {
        this.initialize();
        return;
      }
      if (!responses || !responses.completed$) {
        // this got triggered, but not by the upload complete callback, ignore
        return;
      }
      delete responses.completed$;
      const importedIntegrations: Integrations = [];
      const errors = [];
      // find the imported integrations via the ID in the returned response
      Object.keys(responses).forEach(fileName => {
        const response = this.responses[fileName];
        if (response.status === 200) {
          const apiResponse = JSON.parse(response.response)[0];
          const integration = integrations.find(i => i.id === apiResponse.id);
          if (integration) {
            importedIntegrations.push(integration);
          }
        } else {
          errors.push({
            fileName,
            ...response
          });
        }
      });
      const referencedConnections = [];
      // look through the imported integrations and pull out referenced
      // connections, so we can show if any connections need configuration
      importedIntegrations.forEach(i => {
        i.flows.forEach(f => {
          f.steps.filter(s => s.stepKind === 'endpoint').forEach(step => {
            if (!step.connection) {
              return;
            }
            if (!referencedConnections.find(c => c.id === step.connection.id)) {
              referencedConnections.push(step.connection);
            }
          });
        });
      });
      const importedConnections = referencedConnections.map(connection => {
        return connections.find(c => c.id === connection.id);
      });
      this.errors$.next(errors);
      this.importedIntegrations$.next(importedIntegrations);
      this.importedConnections$.next(importedConnections);
      if (importedIntegrations.length || importedConnections.length) {
        this._showReviewStep = true;
      }
    });

    this.integrationStore.loadAll();
    this.connectionStore.loadAll();

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

    this.uploader.onAfterAddingAll = fileItems => {
      // trigger initialization
      this.responses$.next({ initialize$: true });
      return {fileItems};
    };

    this.uploader.onWhenAddingFileFailed = (
      _item: FileLikeObject,
      _filter: any,
      _options: any
    ): any => {
      this.fileSelect.nativeElement['value'] = '';
      this.uploader.clearQueue();
    };

    // Collect the results of each individual upload
    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number
    ) => {
        this.responses[item.file.name] = { item, status, response };
    };

    // this fires when all uploads are complete, regardless if it's successful or not
    this.uploader.onCompleteAll = () => {
      // give a teeny bit of extra time for other events to fire
      setTimeout(() => {
        this.responses.completed$ = true;
        this.responses$.next(this.responses);
      }, 1000);
    };
  }

  redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
