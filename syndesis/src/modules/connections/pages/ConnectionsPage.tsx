import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import { ConnectionsWithToolbar } from '../containers/ConnectionsWithToolbar';

export function getConnectionHref(
  baseUrl: string,
  connection: Connection
): string {
  return `${baseUrl}/${connection.id}`;
}

export default class ConnectionsPage extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <WithConnections>
            {({ data, hasData, error }) => (
              <ConnectionsWithToolbar
                error={error}
                loading={!hasData}
                connections={data.connectionsForDisplay}
                getConnectionHref={getConnectionHref.bind(null, match.url)}
              />
            )}
          </WithConnections>
        )}
      </WithRouter>
    );
  }
}
