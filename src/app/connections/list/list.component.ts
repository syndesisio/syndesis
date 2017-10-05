import {
  Component,
  Input,
  Output,
  OnInit,
  EventEmitter,
  ViewChild,
  ChangeDetectorRef,
} from '@angular/core';

import { NotificationService, NotificationType } from 'patternfly-ng';

import { ModalService } from '../../common/modal/modal.service';
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

  @Input() connections: Connections;
  @Input() loading: boolean;
  @Input() showKebab = true;
  @Input() isConnectors = false;
  @Input() showNewConnection = false;
  @Output() onSelected: EventEmitter<Connection> = new EventEmitter();

  constructor(
    public store: ConnectionStore,
    public detector: ChangeDetectorRef,
    private notificationService: NotificationService,
    private modalService: ModalService,
  ) {}

  /**
   * Actual delete action once the user confirms
   * @param {Connection} connection
   */
  deleteAction(connection: Connection) {
    const sub = this.store.delete(connection).subscribe(
      () => {
        sub.unsubscribe();
        setTimeout(() => {
          this.popNotification({
            type: NotificationType.SUCCESS,
            header: 'Delete Successful',
            message: 'Connection successfully deleted.',
            showClose: true,
          });
        }, 10);
      },
      (err: any) => {
        sub.unsubscribe();
        setTimeout(() => {
          this.popNotification({
            type: NotificationType.DANGER,
            header: 'Delete Failed',
            message: `Failed to delete connection: ${err}`,
            showClose: true,
          });
        }, 10);
      },
    );
  }

  /**
   * Open modal to confirm delete
   * @param {Connection} connection
   * @param $event
   */
  requestDelete(connection: Connection, $event) {
    log.debugc(() => 'Selected connection for delete: ' + connection.id);
    this.selectedForDelete = connection;
    this.modalService
      .show()
      .then(modal => (modal.result ? this.deleteAction(connection) : false));
  }

  /**
   * Selecting a Connection
   * @param {Connection} connection
   */
  onSelect(connection: Connection) {
    if (connection) {
      log.debugc(
        () => 'Selected connection (list): ' + connection.name,
        category,
      );
      this.selectedId = connection.id;
    }
    this.onSelected.emit(connection);
  }

  isSelected(connection: Connection) {
    return connection.id === this.selectedId;
  }

  /**
   * Initialization
   */
  ngOnInit() {
    log.debugc(
      () =>
        'Got connections: ' + JSON.stringify(this.connections, undefined, 2),
      category,
    );
  }

  /**
   * Toast notification
   * @param notification
   */
  popNotification(notification) {
    this.notificationService.message(
      notification.type,
      notification.header,
      notification.message,
      false,
      null,
      [],
    );
  }
}
