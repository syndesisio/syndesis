import { Component, OnInit } from '@angular/core';

import { CurrentConnectionService } from '../current-connection';
import { Connection } from '../../../model';

@Component({
  selector: 'ipaas-connections-connection-basics',
  templateUrl: 'connection-basics.component.html',
})
export class ConnectionsConnectionBasicsComponent implements OnInit {
  constructor(
    private current: CurrentConnectionService,
  ) { }

  get connection(): Connection {
    return this.current.connection;
  }

  set connection(connection: Connection) {
    this.current.connection = connection;
  }

  ngOnInit() { }
}
