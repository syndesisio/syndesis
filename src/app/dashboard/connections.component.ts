import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { ModalDirective } from 'ng2-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';

import { log, getCategory } from '../logging';

import { Connection, Connections } from '../model';
import { ConnectionStore } from '../store/connection/connection.store';

const category = getCategory('Dashboard');

@Component({
  selector: 'ipaas-dashboard-connections',
  templateUrl: './connections.component.html',
  styleUrls: ['./connections.component.scss'],
})
export class DashboardConnectionsComponent implements OnInit {

  private toasterService: ToasterService;
  private toast;

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() connections: Connections;
  @Input() loading: boolean;
  truncateLimit = 80;
  truncateTrail = '…';

  constructor(toasterService: ToasterService) {
    this.toasterService = toasterService;
  }


  //-----  Delete ------------------->>

  // Actual delete action once the user confirms
  deleteAction(connection: Connections) {
    log.debugc(() => 'Selected connection for delete: ' + JSON.stringify(connection['id']));

    this.hideModal();

    //this.store.deleteEntity(connection['id']);

    this.toast = {
      type: 'success',
      title: 'Delete Successful',
      body: 'Connection successfully deleted.',
    };

    setTimeout(this.popToast(this.toast), 1000);
  }

  // Open modal to confirm delete
  requestDelete(connection: Connections) {
    log.debugc(() => 'Selected connection for delete: ' + JSON.stringify(connection['id']));
    this.showModal();
  }


  //-----  Duplicate ------------------->>

  duplicate(connection: Connections) {
    log.debugc(() => 'Request to duplicate the following connection: ' + JSON.stringify(connection['id']));
  }


  //-----  Modals ------------------->>

  public showModal(): void {
    this.childModal.show();
  }

  public hideModal(): void {
    this.childModal.hide();
  }

  //-----  Toast ------------------->>

  // Show toast notification
  popToast(toast) {
    this.toasterService.pop(toast);
  }


  //----- Initialization ------------------->>

  ngOnInit() {
    log.debugc(() => 'Got connections: ' + JSON.stringify(this.connections, undefined, 2), category);
  }

}
