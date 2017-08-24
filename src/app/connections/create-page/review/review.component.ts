import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterStateSnapshot } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { CurrentConnectionService, ConnectionEvent } from '../current-connection';
import { Connection } from '../../../model';
import { CanComponentDeactivate } from '../../../common/can-deactivate-guard.service';
import { ModalService } from '../../../common/modal/modal.service';

@Component({
  selector: 'syndesis-connections-review',
  templateUrl: 'review.component.html',
})
export class ConnectionsReviewComponent implements OnInit, OnDestroy, CanComponentDeactivate {

  private subscription: Subscription;
  private saved = false;
  private loaded = false;

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

  ngOnInit() {
    this.subscription = this.current.events.subscribe((event: ConnectionEvent) => {
      if (event.kind === 'connection-set-connection') {
        this.loaded = true;
      }
      if (event.kind === 'connection-save-connection') {
        this.saved = true;
      }
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  canDeactivate(nextState: RouterStateSnapshot) {
    return this.saved ||
           nextState.url === '/connections/create/connection-basics' ||
           nextState.url === '/connections/create/cancel' ||
           nextState.url === '/connections/create/configure-fields' ||
           this.modalService.show().then(modal => modal.result);
  }
}
