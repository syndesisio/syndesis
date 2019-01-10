import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { Action, ConnectionOverview } from '@syndesis/models';
import {
  Breadcrumb,
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  PageHeader,
} from '@syndesis/ui';
import { WithRouter } from '@syndesis/utils';
import * as H from 'history';
import { reverse } from 'named-urls';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import { ConnectionsWithToolbar } from '../../connections/containers/ConnectionsWithToolbar';
import routes from '../routes';

export function getFinishSelectActionHref(
  locationState: any,
  connection: Connection
): H.LocationDescriptor {
  return {
    pathname: reverse(routes.integrations.create.finish.selectAction, {
      connectionId: connection.id,
    }),
    state: {
      ...locationState,
      finishConnection: connection,
    },
  };
}

export class IntegrationCreatorFinishConnectionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouter>
          {({ location }) => {
            const startConnection: ConnectionOverview =
              location.state.startConnection;
            const startAction: Action = location.state.startAction;
            return (
              <ContentWithSidebarLayout
                sidebar={
                  <IntegrationVerticalFlow disabled={true}>
                    {({ expanded }) => (
                      <>
                        <IntegrationFlowStepWithOverview
                          icon={
                            <img
                              src={startConnection.icon}
                              width={24}
                              height={24}
                            />
                          }
                          i18nTitle={`1. ${startAction.name}`}
                          i18nTooltip={`1. ${startAction.name}`}
                          active={false}
                          showDetails={expanded}
                          name={startConnection.connector!.name}
                          action={startAction.name}
                          dataType={'TODO'}
                        />
                        <IntegrationFlowStepGeneric
                          icon={'+'}
                          i18nTitle={'2. Finish'}
                          i18nTooltip={'Finish'}
                          active={true}
                          showDetails={expanded}
                          description={'Choose a connection'}
                        />
                      </>
                    )}
                  </IntegrationVerticalFlow>
                }
                content={
                  <>
                    <PageHeader>
                      <Breadcrumb>
                        <Link to={routes.integrations.list}>Integrations</Link>
                        <Link
                          to={routes.integrations.create.start.selectConnection}
                        >
                          New integration
                        </Link>
                        <Link
                          to={reverse(
                            routes.integrations.create.start.selectAction,
                            {
                              connectionId: startConnection.id,
                            }
                          )}
                        >
                          Start connection
                        </Link>
                        <Link
                          to={reverse(
                            routes.integrations.create.start.configureAction,
                            {
                              actionId: startAction.id,
                              connectionId: startConnection.id,
                            }
                          )}
                        >
                          Configure action
                        </Link>
                        <span>Finish Connection</span>
                      </Breadcrumb>
                      <h1>Choose a Finish Connection</h1>
                      <p>
                        Click the connection that completes the integration. If
                        the connection you need is not available, click Create
                        Connection.
                      </p>
                    </PageHeader>
                    <WithConnections>
                      {({ data, hasData, error }) => (
                        <ConnectionsWithToolbar
                          error={error}
                          loading={!hasData}
                          connections={data.connectionsWithToAction}
                          getConnectionHref={getFinishSelectActionHref.bind(
                            null,
                            location.state
                          )}
                        />
                      )}
                    </WithConnections>
                  </>
                }
              />
            );
          }}
        </WithRouter>
      </WithClosedNavigation>
    );
  }
}
