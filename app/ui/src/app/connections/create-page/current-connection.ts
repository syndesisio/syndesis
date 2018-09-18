import { EventEmitter, Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { getCategory, log } from '@syndesis/ui/logging';

import {
  ConfiguredConfigurationProperty,
  Connection,
  I18NService
} from '@syndesis/ui/platform';
import { ConnectionStore, ConnectorStore } from '@syndesis/ui/store';
import { Observable, EMPTY, Subscription } from 'rxjs';
import { catchError, mergeMap } from 'rxjs/operators';

const category = getCategory('CurrentConnectionService');

export class ConnectionEvent {
  kind: string;
  [name: string]: any;
}

@Injectable()
export class CurrentConnectionService {
  events = new EventEmitter<ConnectionEvent>();

  private _loaded: boolean;
  private _connection: Connection;
  private _credentials: any;
  private _oauthStatus: any;
  private _formGroup: FormGroup;
  private _saving: boolean;
  private subscription: Subscription;

  constructor(
    private store: ConnectionStore,
    private connectorStore: ConnectorStore,
    private i18NService: I18NService
  ) {}

  init() {
    this._credentials = undefined;
    this._oauthStatus = undefined;
    this._connection = undefined;
    this._formGroup = undefined;
    this._loaded = false;
    this.subscription = this.events.subscribe((event: ConnectionEvent) =>
      this.handleEvent(event)
    );
  }

  get saving() {
    return this._saving;
  }

  dispose() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  handleEvent(event: ConnectionEvent) {
    // log.info(() => 'connection event: ' + JSON.stringify(event), category);
    switch (event.kind) {
      case 'connection-check-connector':
        if (!this.fetchConnector(this._connection.connectorId)) {
          this.events.emit({
            kind: 'connection-check-credentials',
            connectorId: this._connection.connectorId
          });
        }
        break;
      case 'connection-check-credentials':
        if (!this.checkCredentials()) {
          this.events.emit({
            kind: 'connection-set-connection',
            connection: this._connection
          });
        }
        break;
      case 'connection-set-connection':
        this._loaded = true;
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

  hasConnector() {
    return this.connection.connectorId !== undefined;
  }

  acquireCredentials() {
    if (!this._connection || !this._connection.connectorId) {
      this._credentials = undefined;
      return EMPTY;
    }
    const connectorId = this._connection.connectorId;
    return this.connectorStore.acquireCredentials(connectorId)
      .pipe(
        mergeMap((resp: any) => {
          log.info(() => 'Got response: ' + JSON.stringify(resp));
          return resp;
        }),
        catchError((error: any) => {
          const data = error.data;
          const message = data && data.userMsgDetail ?
            this.i18NService.localize('connections.external-oauth-error', data.userMsgDetail) :
            this.i18NService.localize('connetions.unknown-oauth-error');
          this._oauthStatus = {
            message: message
          };
          log.info(() => 'Error response initiating oauth flow:' + JSON.stringify(error));
          return error;
        })
      );
  }

  clearOAuthError() {
    this.oauthStatus = undefined;
  }

  hasCredentials(): boolean {
    return this._credentials && this._credentials.type !== undefined;
  }

  hasProperties(): boolean {
    if (!this.hasConnector()) {
      return false;
    }
    const props = this.connection.connector.properties || {};
    return Object.keys(props).length > 0;
  }

  get oauthError(): boolean {
    return this._oauthStatus && this._oauthStatus !== 'SUCCESS';
  }

  get oauthStatus(): any {
    return this._oauthStatus;
  }

  set oauthStatus(oauthStatus: any) {
    this._oauthStatus = oauthStatus;
  }

  get loaded(): boolean {
    return this._loaded;
  }

  get credentials(): any {
    return this._credentials;
  }

  get connection(): Connection {
    return this._connection;
  }

  get formGroup(): FormGroup {
    return this._formGroup;
  }

  set formGroup(formGroup: FormGroup) {
    this._formGroup = formGroup;
  }

  set connection(connection: Connection) {
    this._loaded = false;
    this._connection = connection;
    this.events.emit({
      kind: 'connection-check-connector',
      connection: this._connection
    });
  }

  private checkCredentials() {
    const connectorId = this._connection.connectorId;
    if (!connectorId) {
      return false;
    }
    if (!this._credentials || this._credentials.connectorId !== connectorId) {
      // fetch any credentials for the connector
      const sub = this.fetchCredentials().subscribe(
        () => {
          sub.unsubscribe();
          this.events.emit({
            kind: 'connection-set-connection',
            connection: this._connection
          });
        },
        error => {
          log.info(
            () =>
              'Failed to fetch connector credentials: ' + JSON.stringify(error),
            category
          );
          sub.unsubscribe();
          this.events.emit({
            kind: 'connection-set-connection',
            connection: this._connection
          });
        }
      );
      return true;
    } else {
      return false;
    }
  }

  private fetchConnector(connectorId: string) {
    if (connectorId && !this._connection.connector) {
      const sub = this.connectorStore.load(connectorId).subscribe(
        connector => {
          if (!connector.id) {
            return;
          }
          this._connection.connector = connector;
          this._connection.icon = connector.icon;
          this.events.emit({
            kind: 'connection-check-credentials',
            connection: this._connection
          });
          sub.unsubscribe();
        },
        error => {
          try {
            log.info(
              () => 'Failed to fetch connector: ' + JSON.stringify(error),
              category
            );
          } catch (err) {
            log.info(() => 'Failed to fetch connector: ' + error, category);
          }
          this.events.emit({
            kind: 'connection-check-credentials',
            error: error,
            connection: this._connection
          });
          sub.unsubscribe();
        }
      );
      return true;
    }
    return false;
  }

  private fetchCredentials() {
    if (!this._connection || !this._connection.connectorId) {
      this._credentials = undefined;
      return EMPTY;
    }
    const connectorId = this._connection.connectorId;
    return Observable.create(observer => {
      this.connectorStore.credentials(connectorId).subscribe((resp: any) => {
        // enrich the response with the connectorId
        this._credentials = { ...resp, ...{ connectorId: connectorId } };
        observer.next(this._credentials);
        observer.complete();
      });
    });
  }

  private saveConnection(event: ConnectionEvent) {
    this._saving = true;
    const connection = { ...(event.connection || this.connection) };
    // properties can be unset
    const properties = { ...connection.connector.properties };
    // strip out any values that happen to have been set in the `properties` object
    for (const key of Object.keys(properties)) {
      const {
        value,
        ...property
      } = <ConfiguredConfigurationProperty>properties[key];
      properties[key] = property;
    }
    connection.connectorId = connection.connector.id;
    connection.connector.properties = properties;
    const sub = this.store.updateOrCreate(connection).subscribe(
      (c: Connection) => {
        const action = event['action'];
        if (action && typeof action === 'function') {
          action(c);
        }
        this._saving = false;
        sub.unsubscribe();
      },
      (reason: any) => {
        const errorAction = event['error'];
        if (errorAction && typeof errorAction === 'function') {
          errorAction(reason);
        }
        this._saving = false;
        sub.unsubscribe();
      }
    );
  }
}
