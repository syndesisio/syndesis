import { Component, Input, Output, OnInit, EventEmitter, ViewChild } from '@angular/core';
import { ModalDirective } from 'ng2-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';

import { log, getCategory } from '../../logging';
import { Connections, Connection } from '../../model';

const category = getCategory('Connections');

@Component({
  selector: 'ipaas-connections-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class ConnectionsListComponent implements OnInit {

  truncateLimit = 80;
  truncateTrail = '…';
  selectedId = undefined;

  private toasterService: ToasterService;
  private toast;

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() connections: Connections;
  @Input() loading: boolean;
  @Output() onSelected: EventEmitter<Connection> = new EventEmitter();

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


  //-----  Selecting a Connection ------------------->>


  onSelect(connection: Connection) {
    log.debugc(() => 'Selected connection (list): ' + connection.name, category);
    this.selectedId = connection.id;
    this.onSelected.emit(connection);
  }

  isSelected(connection: Connection) {
    return connection.id === this.selectedId;
  }

  //----- Initialization ------------------->>

  ngOnInit() {
    log.debugc(() => 'Got connections: ' + JSON.stringify(this.connections, undefined, 2), category);
  }

}
