import { ConnectionOverview, Connector } from '@syndesis/models';

export function getConnectionIcon(
  connection: ConnectionOverview | Connector,
  publicUrl: string | undefined
): string {
  return (connection.icon || '').startsWith('data:')
    ? connection.icon!
    : `${publicUrl}/icons/${connection.id}.connection.png`;
}
