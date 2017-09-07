import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterStateSnapshot } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import {
  CurrentConnectionService,
  ConnectionEvent,
} from '../current-connection';
import { Connection } from '../../../model';

@Component({
  selector: 'syndesis-connections-connection-basics',
  templateUrl: 'connection-basics.component.html',
})
export class ConnectionsConnectionBasicsComponent implements OnInit, OnDestroy {
  private subscription: Subscription;

  constructor(
    public current: CurrentConnectionService,
    public route: ActivatedRoute,
    public router: Router,
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

  }

  ngOnDestroy() {
  }
}
