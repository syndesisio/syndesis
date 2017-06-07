import { Component, OnInit, OnDestroy, EventEmitter, Input } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { Connection } from '../../model';
import { ConnectionStore } from '../../store/connection/connection.store';
import { CurrentConnectionService } from '../create-page/current-connection';

@Component({
  selector: 'syndesis-connection-view-wrapper',
  templateUrl: './view-wrapper.component.html',
  styleUrls: ['./view-wrapper.component.scss'],
})
export class ConnectionViewWrapperComponent implements OnInit, OnDestroy {

  connection: Observable<Connection>;
  public mode = 'view';

  constructor(
    private store: ConnectionStore,
    ) {
      this.connection = this.store.resource;
    }

  ngOnInit() { }

  ngOnDestroy() { }
}
