import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterStateSnapshot } from '@angular/router';

import {
  CurrentConnectionService,
  ConnectionEvent,
} from '../current-connection';
import { Connection } from '../../../model';
import { CanComponentDeactivate } from '../../../common/can-deactivate-guard.service';

@Component({
  selector: 'syndesis-connections-connection-basics',
  templateUrl: 'connection-basics.component.html',
})
export class ConnectionsConnectionBasicsComponent implements OnInit, CanComponentDeactivate {

  constructor(
    private current: CurrentConnectionService,
    public route: ActivatedRoute,
    public router: Router,
  ) {}

  get connection(): Connection {
    return this.current.connection;
  }

  set connection(connection: Connection) {
    this.current.connection = connection;
  }

  ngOnInit() {
    const subscription = this.current.events.subscribe(
      (event: ConnectionEvent) => {
        switch (event.kind) {
          case 'connection-set-connection':
            this.router.navigate(['..', 'configure-fields'], {
              relativeTo: this.route,
            });
            return;
        }
        subscription.unsubscribe();
      },
    );
  }

  canDeactivate(nextState: RouterStateSnapshot) {
    return nextState.url === '/connections/create/cancel' ||
           nextState.url === '/connections/create/configure-fields' ||
           window.confirm('Discard changes?');
  }
}
