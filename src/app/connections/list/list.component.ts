import {
  Component,
  Input,
  Output,
  OnInit,
  EventEmitter,
  ViewChild,
  ChangeDetectorRef,
} from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { NotificationService, NotificationType } from 'patternfly-ng';

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

  @ViewChild('childModal') public childModal: ModalDirective;

  @Input() connections: Connections;
  @Input() loading: boolean;
  @Input() showKebab = true;
  @Output() onSelected: EventEmitter<Connection> = new EventEmitter();

  constructor(
    public store: ConnectionStore,
    public detector: ChangeDetectorRef,
    private notificationService: NotificationService,
  ) {}

  //-----  Delete ------------------->>

  // Actual delete action once the user confirms
  deleteAction(connection: Connection) {
    this.hideModal();
    const sub = this.store.delete(connection).subscribe(
      () => {
        sub.unsubscribe();
        setTimeout(() => {
          this.popNotification(
            {
              type      : NotificationType.SUCCESS,
              header    : 'Delete Successful',
              message   : 'Connection successfully deleted.',
              showClose : true,
            },
          );
        }, 10);
      },
      (err: any) => {
        sub.unsubscribe();
        setTimeout(() => {
          this.popNotification(
            {
              type      : NotificationType.DANGER,
              header    : 'Delete Failed',
              message   : `Failed to delete connection: ${err}`,
              showClose : true,
            },
          );
        }, 10);
      },
    );
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

  //-----  Selecting a Connection ------------------->>
  onSelect(connection: Connection) {
    log.debugc(
      () => 'Selected connection (list): ' + connection.name,
      category,
    );
    this.selectedId = connection.id;
    this.onSelected.emit(connection);
  }

  isSelected(connection: Connection) {
    return connection.id === this.selectedId;
  }

  //----- Initialization ------------------->>

  ngOnInit() {
    log.debugc(
      () =>
        'Got connections: ' + JSON.stringify(this.connections, undefined, 2),
      category,
    );
  }

  //-----  Toast ------------------->>

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
