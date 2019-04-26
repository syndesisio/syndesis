import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import * as React from 'react';
import { PageTitle } from '../../../shared';
import { ConnectionsWithToolbar } from '../components';
import resolvers from '../resolvers';

function getConnectionHref(connection: Connection) {
  return resolvers.connection.details({ connection });
}

export class ConnectionsPage extends React.Component {
  public render() {
    return (
      <WithConnections>
        {({ data, hasData, error }) => (
          <>
            <PageTitle title={'Connections'} />
            <ConnectionsWithToolbar
              error={error}
              includeConnectionMenu={true}
              loading={!hasData}
              connections={data.connectionsForDisplay}
              getConnectionHref={getConnectionHref}
            />
          </>
        )}
      </WithConnections>
    );
  }
}
