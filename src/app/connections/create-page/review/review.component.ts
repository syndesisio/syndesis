import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterStateSnapshot } from '@angular/router';

import { CurrentConnectionService, ConnectionEvent } from '../current-connection';
import { Connection } from '../../../model';
import { CanComponentDeactivate } from '../../../common/can-deactivate-guard.service';

@Component({
  selector: 'syndesis-connections-review',
  templateUrl: 'review.component.html',
})
export class ConnectionsReviewComponent implements OnInit, CanComponentDeactivate {

  private saved = false;

  constructor(private current: CurrentConnectionService) {}

  get connection(): Connection {
    return this.current.connection;
  }

  set connection(connection: Connection) {
    this.current.connection = connection;
  }

  ngOnInit() {
    const subscription = this.current.events.subscribe((event: ConnectionEvent) => {
      if (event.kind === 'connection-save-connection') {
        this.saved = true;
        subscription.unsubscribe();
      }
    });
  }

  canDeactivate(nextState: RouterStateSnapshot) {
    return this.saved ||
           nextState.url === '/connections/create/cancel' ||
           nextState.url === '/connections/create/configure-fields' ||
           window.confirm('Discard changes?');
  }
}
