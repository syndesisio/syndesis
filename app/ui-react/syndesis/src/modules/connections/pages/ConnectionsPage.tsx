import { WithConnections } from '@syndesis/api';
import * as React from 'react';
import { PageTitle } from '../../../shared';
import { ConnectionsWithToolbar } from '../components';
import resolvers from '../resolvers';

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
              getConnectionHref={connection =>
                resolvers.connection.details({ connection })
              }
              getConnectionEditHref={connection =>
                resolvers.connection.edit({ connection })
              }
            />
          </>
        )}
      </WithConnections>
    );
  }
}
