import { Component, OnInit } from '@angular/core';
import { RouterStateSnapshot } from '@angular/router';

import {
  CurrentConnectionService,
  ConnectionEvent,
} from '../current-connection';
import { Connection } from '../../../model';
import { log } from '../../../logging';
import { CanComponentDeactivate } from '../../../common/can-deactivate-guard.service';
import { ModalService } from '../../../common/modal/modal.service';

@Component({
  selector: 'syndesis-connections-configure-fields',
  templateUrl: 'configure-fields.component.html',
})
export class ConnectionsConfigureFieldsComponent
  implements OnInit, CanComponentDeactivate {
  constructor(
    private current: CurrentConnectionService,
    private modalService: ModalService,
  ) {}

  get connection(): Connection {
    return this.current.connection;
  }

  set connection(connection: Connection) {
    if (!connection.connectorId) {
      return;
    }
    this.current.connection = connection;
  }

  get hasCredentials() {
    return this.current.hasCredentials();
  }

  acquireCredentials() {
    this.current.acquireCredentials();
  }

  ngOnInit() {}

  canDeactivate(nextState: RouterStateSnapshot) {
    return (
      nextState.url === '/connections/create/cancel' ||
      nextState.url === '/connections/create/connection-basics' ||
      nextState.url === '/connections/create/review' ||
      this.modalService.show().then(modal => modal.result)
    );
  }
}
