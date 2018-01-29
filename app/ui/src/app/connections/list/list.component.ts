import {
  Component,
  Input,
  Output,
  OnInit,
  EventEmitter,
  ViewChild
} from '@angular/core';

import { NotificationType } from 'patternfly-ng';

import { ModalService } from '../../common/modal/modal.service';
import { ConnectionStore } from '../../store/connection/connection.store';
import { log, getCategory } from '../../logging';
import { Connections, Connection } from '../../model';
import { NotificationService } from '@syndesis/ui/common/ui-patternfly/notification-service';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connections-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ConnectionsListComponent implements OnInit {
  truncateTrail = '…';
  selectedId: string;
  selectedForDelete: Connection;

  @Input() connections: Connections;
  @Input() loading: boolean;
  @Input() showKebab = true;
  @Input() isConnectors = false;
  @Input() showNewConnection = false;
  @Output() onSelected = new EventEmitter<Connection>();

  constructor(
    public store: ConnectionStore,
    private notificationService: NotificationService,
    private modalService: ModalService
  ) {
    this.notificationService.setDelay(4000);
  }

  //----- Initialization ------------------->>

  ngOnInit() {
    log.debugc(
      () =>
        'Got connections: ' + JSON.stringify(this.connections, undefined, 2),
      category
    );
  }

  //-----  Delete ------------------->>

  // Actual delete action once the user confirms
  deleteAction(connection: Connection) {
    const sub = this.store.delete(connection).subscribe(
      () => {
        sub.unsubscribe();
        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Delete Successful',
          message: 'Connection successfully deleted.'
        });
      },
      (err: any) => {
        sub.unsubscribe();
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Delete Failed',
          message: `Failed to delete connection: ${err}`,
          isPersistent: true
        });
      }
    );
  }

  // Open modal to confirm delete
  requestDelete(connection: Connection, $event) {
    log.debugc(() => 'Selected connection for delete: ' + connection.id);
    this.selectedForDelete = connection;
    this.modalService
      .show()
      .then(modal => (modal.result ? this.deleteAction(connection) : false));
  }

  //-----  Selecting a Connection ------------------->>
  onSelect(connection: Connection) {
    if (connection) {
      log.debugc(
        () => 'Selected connection (list): ' + connection.name,
        category
      );
      this.selectedId = connection.id;
    }
    this.onSelected.emit(connection);
  }
}
