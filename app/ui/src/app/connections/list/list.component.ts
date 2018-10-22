import {
  Component,
  Input,
  Output,
  OnInit,
  EventEmitter
} from '@angular/core';

import { NotificationType } from 'patternfly-ng';
import { Connections, Connection } from '@syndesis/ui/platform';
import { ModalService } from '@syndesis/ui/common/modal/modal.service';
import { ConnectionStore } from '@syndesis/ui/store/connection/connection.store';
import { log, getCategory } from '@syndesis/ui/logging';
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
    log.debug(
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
    log.debug(() => 'Selected connection for delete: ' + connection.id);
    this.selectedForDelete = connection;
    this.modalService
      .show()
      .then(modal => (modal.result ? this.deleteAction(connection) : false));
  }

  //-----  Selecting a Connection ------------------->>
  onSelect(connection: Connection) {
    if (connection) {
      log.debug(
        () => 'Selected connection (list): ' + connection.name,
        category
      );
      this.selectedId = connection.id;
    }
    this.onSelected.emit(connection);
  }

  /**
   * Indicates if the connection is being used in any integrations.
   *
   * @param connection the connection being checked
   * @returns {boolean} `true` if used in an integration
   */
  isBeingUsed( connection: any ): boolean {
    return connection.uses ? connection.uses > 0 : false;
  }
}
