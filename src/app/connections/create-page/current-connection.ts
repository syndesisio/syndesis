import { Injectable, EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';

import { ConnectionStore } from '../../store/connection/connection.store';
import { ConnectorStore } from '../../store/connector/connector.store';
import { Connection, Connector, Connections, Connectors } from '../../model';

import { log, getCategory } from '../../logging';

const category = getCategory('CurrentConnectionService');

export class ConnectionEvent {
  kind: string;
  [name: string]: any;
}

@Injectable()
export class CurrentConnectionService {
  private _connection: Connection;
  private _credentials: any;
  private subscription: Subscription;

  events = new EventEmitter<ConnectionEvent>();

  constructor(
    private store: ConnectionStore,
    private connectorStore: ConnectorStore,
  ) {
    this.subscription = this.events.subscribe((event: ConnectionEvent) =>
      this.handleEvent(event),
    );
  }

  handleEvent(event: ConnectionEvent) {
    switch (event.kind) {
      case 'connection-set-connection':
        break;
      // TODO not sure if these next 3 cases are needed really
      case 'connection-set-name':
        this._connection.name = event['name'];
        break;
      case 'connection-set-description':
        this._connection.description = event['description'];
        break;
      case 'connection-set-tags':
        this._connection.tags = event['tags'];
        break;
      case 'connection-save-connection':
        this.saveConnection(event);
        break;
      default:
    }
  }

  private fetchCredentials() {
    if (!this._connection || !this._connection.connectorId) {
      this._credentials = undefined;
      return Observable.empty();
    }
    const connectorId = this._connection.connectorId;
    return Observable.create(observer => {
      this.connectorStore
        .credentials(connectorId)
        .subscribe((resp: any) => {
          // enrich the response with the connectorId
          this._credentials = { ...resp, ...{ connectorId: connectorId } };
          observer.next(this._credentials);
          observer.complete();
        });
    });
  }

  public acquireCredentials() {
    if (!this._connection || !this._connection.connectorId) {
      this._credentials = undefined;
      return Observable.empty();
    }
    const connectorId = this._connection.connectorId;
    this.connectorStore.acquireCredentials(connectorId).subscribe((resp: any) => {
      console.log('Got back: ', resp);
    });
  }

  private saveConnection(event: ConnectionEvent) {
    // poor man's clone
    const connection = <Connection>JSON.parse(
      JSON.stringify(event['connection'] || this.connection),
    );
    // just in case this leaks through from the form
    for (const prop in connection.connector.properties) {
      if (!prop.hasOwnProperty(prop)) {
        continue;
      }
      delete connection.connector.properties[prop]['value'];
    }
    const sub = this.store.updateOrCreate(connection).subscribe(
      (c: Connection) => {
        log.debugc(
          () => 'Saved connection: ' + JSON.stringify(c, undefined, 2),
          category,
        );
        const action = event['action'];
        if (action && typeof action === 'function') {
          action(c);
        }
        sub.unsubscribe();
      },
      (reason: any) => {
        log.debugc(
          () =>
            'Error saving connection: ' + JSON.stringify(reason, undefined, 2),
          category,
        );
        const errorAction = event['error'];
        if (errorAction && typeof errorAction === 'function') {
          errorAction(reason);
        }
        sub.unsubscribe();
      },
    );
  }

  get credentials(): any {
    return this._credentials;
  }

  hasCredentials(): boolean {
    return this._credentials && this._credentials.type !== undefined;
  }


  get connection(): Connection {
    return this._connection;
  }

  set connection(connection: Connection) {
    this._connection = connection;
    const connectorId = connection.connectorId;
    // only query for credentials if the stored ones don't match the passed in connector
    if (
      !connection ||
      !connectorId ||
      (this._credentials && this._credentials.connectorId === connectorId)
    ) {
      this.events.emit({
        kind: 'connection-set-connection',
        connection: this._connection,
      });
      return;
    }
    // fetch any credentials for the connector
    const sub = this.fetchCredentials().subscribe(
      () => {
        sub.unsubscribe();
        this.events.emit({
          kind: 'connection-set-connection',
          connection: this._connection,
        });
      },
      error => {
        log.infoc(
          () =>
            'Failed to fetch connector credentials: ' + JSON.stringify(error),
          category,
        );
        sub.unsubscribe();
        this.events.emit({
          kind: 'connection-set-connection',
          connection: this._connection,
        });
      },
    );
  }
}
