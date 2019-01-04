import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { Breadcrumb, PageHeader } from '@syndesis/ui';
import { reverse } from 'named-urls';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { ConnectionsWithToolbar } from '../../connections/containers/ConnectionsWithToolbar';
import routes from '../routes';

export function getStartSelectActionHref(connection: Connection): string {
  return reverse(routes.integrations.create.start.selectAction, {
    connectionId: connection.id,
  });
}

export class IntegrationCreatorStartConnectionPage extends React.Component {
  public render() {
    return (
      <>
        <PageHeader>
          <Breadcrumb>
            <Link to={routes.integrations.list}>Integrations</Link>
            <span>New integration</span>
          </Breadcrumb>
          <h1>Choose a Start Connection</h1>
          <p>
            Click the connection that starts the integration. If the connection
            you need is not available, click Create Connection.
          </p>
        </PageHeader>
        <WithConnections>
          {({ data, hasData, error }) => (
            <ConnectionsWithToolbar
              error={error}
              loading={!hasData}
              connections={data.connectionsWithFromAction}
              getConnectionHref={getStartSelectActionHref}
            />
          )}
        </WithConnections>
      </>
    );
  }
}
