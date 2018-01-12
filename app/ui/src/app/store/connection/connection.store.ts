import { Injectable } from '@angular/core';
import { ConnectionService } from './connection.service';
import { Connections, Connection } from '../../model';

import { AbstractStore } from '../entity/entity.store';
import { EventsService } from '../entity/events.service';
import { Observable } from 'rxjs/Observable';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class ConnectionStore extends AbstractStore<
  Connection,
  Connections,
  ConnectionService
> {
  private apiEndpoint: String;

  constructor(
    connectionService: ConnectionService,
    eventService: EventsService,
    config: ConfigService
  ) {
    super(connectionService, eventService, [], <Connection>{});
    this.apiEndpoint = config.getSettings().apiEndpoint;
  }

  protected get kind() {
    return 'Connection';
  }

  protected postProcessEntity(connection: Connection) {
    if (connection.icon.startsWith('db:')) {
      connection.iconPath = `${this.apiEndpoint}/connectors/${connection.connectorId || connection.id}/icon`;
    } else {
      connection.iconPath = `../../../assets/icons/${connection.connectorId || connection.id}.connection.png`;
    }

    return connection;
  }

}
