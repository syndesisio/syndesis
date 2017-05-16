import { Component, Input, Output, OnInit, EventEmitter, ViewChild, ChangeDetectorRef } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { ToasterService } from 'angular2-toaster';

import { ConnectionStore } from '../../store/connection/connection.store';
import { log, getCategory } from '../../logging';
import { Connections, Connection } from '../../model';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connections-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class ConnectionsListComponent implements OnInit {

  truncateTrail = '…';
  selectedId = undefined;
  selectedForDelete: Connection = undefined;

  private toast;

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() connections: Connections;
  @Input() loading: boolean;
  @Input() showKebab = true;
  @Output() onSelected: EventEmitter<Connection> = new EventEmitter();

  constructor(
    public toasterService: ToasterService,
    public store: ConnectionStore,
    public detector: ChangeDetectorRef,
    ) { }


  //-----  Delete ------------------->>

  // Actual delete action once the user confirms
  deleteAction(connection: Connection) {
    this.hideModal();
    const sub = this.store.delete(connection).subscribe(() => {
      const toast = {
        type: 'success',
        title: 'Delete Successful',
        body: 'Connection successfully deleted.',
      };
      sub.unsubscribe();
      setTimeout(() => {
        this.toasterService.pop(toast);
      }, 10);
    }, (err: any) => {
      const toast = {
        type: 'error',
        title: 'Delete Failed',
        body: 'Failed to delete connection: ', err,
      };
      sub.unsubscribe();
      setTimeout(() => {
        this.toasterService.pop(toast);
      }, 10);
    });
  }

  // Open modal to confirm delete
  requestDelete(connection: Connection, $event) {
    log.debugc(() => 'Selected connection for delete: ' + connection.id);
    this.selectedForDelete = connection;
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
