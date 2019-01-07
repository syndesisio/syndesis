import { WithConnections, WithIntegrationHelpers } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { Breadcrumb, PageHeader } from '@syndesis/ui';
import { reverse } from 'named-urls';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { ConnectionsWithToolbar } from '../../connections/containers/ConnectionsWithToolbar';
import routes from '../routes';

export function getFinishSelectActionHref(connection: Connection): string {
  return reverse(routes.integrations.create.finish.selectAction, {
    connectionId: connection.id,
  });
}

export class IntegrationCreatorFinishConnectionPage extends React.Component {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ getCreationDraft }) => {
          const integration = getCreationDraft();
          return (
            <>
              <PageHeader>
                <Breadcrumb>
                  <Link to={routes.integrations.list}>Integrations</Link>
                  <Link to={routes.integrations.create.begin}>
                    New integration
                  </Link>
                  <Link
                    to={reverse(routes.integrations.create.start.selectAction, {
                      connectionId: integration.flows![0].steps![0].connection!
                        .id,
                    })}
                  >
                    Start connection
                  </Link>
                  <Link
                    to={reverse(
                      routes.integrations.create.start.configureAction,
                      {
                        actionId: integration.flows![0].steps![0].action!.id,
                        connectionId: integration.flows![0].steps![0]
                          .connection!.id,
                      }
                    )}
                  >
                    Configure action
                  </Link>
                  <span>Finish Connection</span>
                </Breadcrumb>
                <h1>Choose a Finish Connection</h1>
                <p>
                  Click the connection that completes the integration. If the
                  connection you need is not available, click Create Connection.
                </p>
              </PageHeader>
              <WithConnections>
                {({ data, hasData, error }) => (
                  <ConnectionsWithToolbar
                    error={error}
                    loading={!hasData}
                    connections={data.connectionsWithToAction}
                    getConnectionHref={getFinishSelectActionHref}
                  />
                )}
              </WithConnections>
            </>
          );
        }}
      </WithIntegrationHelpers>
    );
  }
}
