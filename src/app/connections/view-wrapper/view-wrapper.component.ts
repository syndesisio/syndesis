import { Component, OnInit, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Connection } from '../../model';
import { ConnectionStore } from '../../store/connection/connection.store';
import { CurrentConnectionService } from '../create-page/current-connection';

@Component({
  selector: 'syndesis-connection-view-wrapper',
  templateUrl: './view-wrapper.component.html',
  styleUrls: ['./view-wrapper.component.scss'],
})
export class ConnectionViewWrapperComponent implements OnInit {
  connection: Observable<Connection>;

  public mode = 'view';

  constructor(
    private store: ConnectionStore,
    public current: CurrentConnectionService,
    public detector: ChangeDetectorRef,
    ) { }

  get conn() {
    return this.current.connection;
  }

  set conn(connection: Connection) {
    this.current.connection = connection;
  }

  ngOnInit() {
    this.store.resource.subscribe((connection) => {
      this.current.connection = connection;
      this.detector.detectChanges();
    });
  }
}
