import { Component, OnInit } from '@angular/core';

import { CurrentConnectionService } from './current-connection';
import { Connection, TypeFactory } from '../../model';

@Component({
  selector: 'ipaas-connection-create-page',
  templateUrl: 'create-page.component.html',
})
export class ConnectionsCreatePage implements OnInit {
  constructor(
    private current: CurrentConnectionService,
  ) { }

  get connection(): Connection {
    return this.current.connection;
  }

  ngOnInit() {
    this.current.connection = TypeFactory.createConnection();
   }
}
