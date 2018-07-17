import { map } from 'rxjs/operators';
import { Component, ElementRef, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';

import {
  IntegrationOverviews,
  IntegrationSupportService,
  Integrations,
  Connections,
  StringMap
} from '@syndesis/ui/platform';
import { FileError, IntegrationImportsData } from './integration-import.models';
import { Observable, Subscription } from 'rxjs';

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
import { filter } from '../../../../../node_modules/rxjs-compat/operator/filter';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './integration-import.component.html',
  styleUrls: ['./integration-import.component.scss']
})
export class IntegrationImportComponent implements OnInit, OnDestroy {
  error: FileError;
  showButtons = false;
  showReviewStep = false;
  uploader: FileUploader;
  hasBaseDropZoneOver: boolean;
  responses: StringMap<any>;
  subscription: Subscription;
  imported: Integrations;
  connections: Connections;

  @ViewChild('fileSelect') fileSelect: ElementRef;

  constructor(
    private integrationSupportService: IntegrationSupportService,
    private integrationStore: IntegrationStore,
    private router: Router,
    public notificationService: NotificationService,
    private tokenExtractor: HttpXsrfTokenExtractor
  ) { }

  cancel() {
    this.redirectBack();
  }

  done() {
    if (
      this.imported &&
      this.imported.length === 1 &&
      this.imported[0].id
    ) {
      this.router.navigate(['/integrations', this.imported[0].id]);
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

  onFileOver(e) {
    this.hasBaseDropZoneOver = e;
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  ngOnInit() {
    this.responses = {};
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
      _item: FileLikeObject,
      _filter: any,
      _options: any
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
      if (this.subscription) {
        this.subscription.unsubscribe();
      }
      // fetch and filter the integrations to the newly imported ones
      this.subscription = this.integrationStore.list.pipe(
        map(integrations => {
          const answer: Integrations = [];
          Object.keys(this.responses).forEach(fileName => {
            const response = this.responses[fileName];
            if (response.status === 200) {
              const apiResponse = JSON.parse(response.response)[0];
              const integration = integrations.find(i => i.id === apiResponse.id);
              if (integration) {
                answer.push(integration);
              }
            }
          });
          return answer;
        }),
      ).subscribe(imported => {
        this.imported = imported;
        // build an of unique connections for each integration
        const connections = [];
        imported.forEach(i => {
          i.steps.filter(s => s.stepKind === 'endpoint').forEach(step => {
            if (!step.connection) {
              return;
            }
            if (!connections.find(c => c.id === step.connection.id)) {
              connections.push(step.connection);
            }
          });
        });
        this.connections = connections;
        this.showButtons = true;
        this.showReviewStep = true;
      });
      // make the call
      this.integrationStore.loadAll();
    };
  }

  redirectBack(): void {
    this.router.navigate(['/integrations']);
  }
}
