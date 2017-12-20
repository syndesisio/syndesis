import { Component, OnInit, ApplicationRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { FilterField, NotificationType } from 'patternfly-ng';
import { IntegrationStore, IntegrationSupportService, ChangeEvent } from '@syndesis/ui/store';
import { Integrations } from '@syndesis/ui/model';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import {
  FileUploader,
  FileItem,
  ParsedResponseHeaders
} from 'ng2-file-upload';

@Component({
  selector: 'syndesis-integrations-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss']
})
export class IntegrationsListPage implements OnInit {
  public uploader: FileUploader;

  loading: Observable<boolean>;
  integrations: Observable<Integrations>;
  filteredIntegrations: Subject<
    Integrations
  > = new BehaviorSubject(<Integrations>{});
  filterFields: Array<FilterField> = [
    {
      id: 'currentStatus',
      title: 'State',
      type: 'select',
      placeholder: 'Filter by state...',
      queries: [
        { id: 'actived', value: 'Active' },
        { id: 'deactivated', value: 'Inactive' },
        { id: 'draft', value: 'Draft' }
      ]
    }
  ];

  constructor(
    private store: IntegrationStore,
    private modalService: ModalService,
    private integrationSupportService: IntegrationSupportService,
    public notificationService: NotificationService,
  ) {
    this.integrations = this.store.list;
    this.loading = this.store.loading;
    this.uploader = new FileUploader({
      url: integrationSupportService.importIntegrationURL(),
      disableMultipart: true,
      autoUpload: true
    });
    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number,
      headers: ParsedResponseHeaders
    ) => {
      if (status === 200) {
        const changeEvents = JSON.parse(response) as ChangeEvent[];
        for ( const x of changeEvents ) {
          this.notificationService.popNotification({
            type: NotificationType.SUCCESS,
            header: 'Imported ' + x.kind,
            message: 'Your ' + x.kind + ' ' + x.id + ' has been imported',
            isPersistent: true,
          });
        }
      } else if (status === 400) {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Import Failed!',
          message: JSON.parse(response).userMsg,
          isPersistent: true,
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

  /**
   * Function that displays a modal for importing a database.
   */
  showImportDialog() {
    this.uploader.clearQueue();
    this.modalService.show('importIntegration');
  }
}
