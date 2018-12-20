import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { Breadcrumb, PageHeader } from '@syndesis/ui';
import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { ConnectionsWithToolbar } from '../../connections/containers/ConnectionsWithToolbar';

export function getConnectionHref(
  baseUrl: string,
  connection: Connection
): string {
  return `${baseUrl}/${connection.id}`;
}

export class IntegrationCreatorSelectConnectionPage extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <>
            <PageHeader>
              <Breadcrumb>
                <Link to={'/'}>Home</Link>
                <Link to={'/integrations'}>Integrations</Link>
                <span>New integration</span>
              </Breadcrumb>
              <h1>Choose a Start Connection</h1>
              <p>
                Click the connection that starts the integration. If the
                connection you need is not available, click Create Connection.
              </p>
            </PageHeader>
            <WithConnections>
              {({ data, hasData, error }) => (
                <ConnectionsWithToolbar
                  error={error}
                  loading={!hasData}
                  connections={data.connectionsWithFromAction}
                  getConnectionHref={getConnectionHref.bind(null, match.url)}
                />
              )}
            </WithConnections>
          </>
        )}
      </WithRouter>
    );
  }
}
