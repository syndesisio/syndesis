import { IConnection, IConnector } from "@syndesis/models";

export function getConnectionIcon(connection: IConnection | IConnector, publicUrl: string | undefined) {
  return connection.icon.startsWith('data:')
    ? connection.icon
    : `${publicUrl}/icons/${connection.id}.connection.png`;
}
