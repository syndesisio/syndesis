import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

import {
  CurrentConnectionService,
  ConnectionEvent,
} from '../current-connection';
import { Connection } from '../../../model';

@Component({
  selector: 'syndesis-connections-connection-basics',
  templateUrl: 'connection-basics.component.html',
})
export class ConnectionsConnectionBasicsComponent implements OnInit {
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
}
