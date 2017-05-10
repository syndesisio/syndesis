import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CurrentConnectionService } from '../current-connection';
import { Connection } from '../../../model';

@Component({
  selector: 'syndesis-connections-configure-fields',
  templateUrl: 'configure-fields.component.html',
})
export class ConnectionsConfigureFieldsComponent implements OnInit {
  constructor(
    private current: CurrentConnectionService,
    private route: ActivatedRoute,
    private router: Router,
  ) { }

  get connection(): Connection {
    return this.current.connection;
  }

  set connection(connection: Connection) {
    this.current.connection = connection;
  }

  ngOnInit() { }
}
