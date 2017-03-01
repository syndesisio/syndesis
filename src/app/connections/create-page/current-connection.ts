import { Injectable, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';

import { ConnectionStore } from '../../store/connection/connection.store';
import { Connection } from '../../model';

export class ConnectionEvent {
  kind: string;
  [name: string]: any;
};

@Injectable()
export class CurrentConnectionService {

  private _connection: Connection;
  private subscription: Subscription;

  events = new EventEmitter<ConnectionEvent>();

  constructor(
    private store: ConnectionStore,
  ) {
    this.subscription = this.events.subscribe((event: ConnectionEvent) => this.handleEvent(event));
  }

  handleEvent(event: ConnectionEvent) {
    switch (event.kind) {
      case 'connection-set-connection':
        break;
      case 'connection-set-name':
        this._connection.name = event['name'];
        break;
      case 'connection-set-description':
        this._connection.description = event['description'];
        break;
      case 'connection-set-tags':
        this._connection.tags = event['tags'];
        break;
      default:
    }

  }

  get connection(): Connection {
    return this._connection;
  }

  set connection(connection: Connection) {
    this._connection = connection;
    this.events.emit({
      kind: 'connection-set-connection',
      connection: this._connection,
    });
  }
}
