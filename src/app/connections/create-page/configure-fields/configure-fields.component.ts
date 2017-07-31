import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { CurrentConnectionService } from '../current-connection';
import { Connection } from '../../../model';

import { log } from '../../../logging';

@Component({
  selector: 'syndesis-connections-configure-fields',
  templateUrl: 'configure-fields.component.html',
})
export class ConnectionsConfigureFieldsComponent implements OnInit {
  constructor(
    private current: CurrentConnectionService,
    private route: ActivatedRoute,
    private router: Router,
    private detector: ChangeDetectorRef,
  ) {}

  get connection(): Connection {
    return this.current.connection;
  }

  set connection(connection: Connection) {
    this.current.connection = connection;
  }

  get hasCredentials() {
    return this.current.hasCredentials();
  }

  acquireCredentials() {
    // TODO
    window.alert('I don\'t work yet');
  }

  ngOnInit() {
    /*
    log.infoc(() => 'Credentials: ' + JSON.stringify(this.current.credentials));
    log.infoc(() => 'hasCredentials: ' + this.current.hasCredentials());
    */
  }
}
