import { WithConnections } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
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
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import { ConnectionsWithToolbar } from '../../connections/containers/ConnectionsWithToolbar';
import resolvers from '../resolvers';

export function getFinishSelectActionHref(
  startConnection: ConnectionOverview,
  startAction: Action,
  integration: Integration,
  connection: ConnectionOverview
): H.LocationDescriptor {
  return resolvers.create.finish.selectAction({
    finishConnection: connection,
    integration,
    startAction,
    startConnection,
  });
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
            const integration: Integration = location.state.integration;
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
                        <Link to={resolvers.list({})}>Integrations</Link>
                        <Link to={resolvers.create.start.selectConnection({})}>
                          New integration
                        </Link>
                        <Link
                          to={resolvers.create.start.selectAction({
                            connection: startConnection,
                          })}
                        >
                          Start connection
                        </Link>
                        <Link
                          to={resolvers.create.start.configureAction({
                            actionId: startAction.id!,
                            connection: startConnection,
                          })}
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
                            startConnection,
                            startAction,
                            integration
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
