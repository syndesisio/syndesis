import { Component, OnInit } from '@angular/core';

import { CurrentConnectionService } from '../current-connection';
import { Connection } from '../../../model';

@Component({
  selector: 'ipaas-connections-configure-fields',
  templateUrl: 'configure-fields.component.html',
})
export class ConnectionsConfigureFieldsComponent implements OnInit {
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
