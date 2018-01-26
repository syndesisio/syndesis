import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { NotificationType } from 'patternfly-ng';
import { IntegrationStore } from '@syndesis/ui/store';
import { IntegrationSupportService } from '../../integration-support.service';
import { Integrations } from '@syndesis/ui/integration';
import { NotificationService } from '@syndesis/ui/common';
import { FileUploader, FileItem } from 'ng2-file-upload';

@Component({
  selector: 'syndesis-import-integration-component',
  templateUrl: './import-integration.component.html',
  styleUrls: ['./import-integration.component.scss']
})
export class IntegrationImportComponent implements OnInit {
  uploader$: FileUploader;
  loading$: Observable<boolean>;
  integrations$: Observable<Integrations>;

  constructor(
    private store: IntegrationStore,
    private integrationSupportService: IntegrationSupportService,
    public notificationService: NotificationService,
  ) {
    this.integrations$ = this.store.list;
    this.loading$ = this.store.loading;
    this.uploader$ = new FileUploader({
      url: integrationSupportService.importIntegrationURL(),
      disableMultipart: true,
      autoUpload: true
    });
    this.uploader$.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number
    ) => {
      if (status === 204) {
        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Imported!',
          message: 'Your integration has been imported'
        });
        /**
         * TODO: For multiple integration upload.
         */
      } else if (status === 400) {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Import Failed!',
          message: JSON.parse(response).userMsg
        });
      } else {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Import Failed!',
          message: 'Your integration could not be imported.'
        });
      }
    };
  }

  ngOnInit() {
    this.store.loadAll();
  }
}
