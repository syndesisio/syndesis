import { WithConnection } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import {
  Breadcrumb,
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
  PageHeader,
} from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import resolvers from '../resolvers';

export class IntegrationCreatorFinishActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouter>
          {({ match, location }) => {
            const startConnection: ConnectionOverview =
              location.state.startConnection;
            const startAction: Action = location.state.startAction;
            const integration: Integration = location.state.integration;
            const finishConnection: ConnectionOverview =
              location.state.finishConnection;
            return (
              <WithConnection
                id={(match.params as any).connectionId}
                initialValue={finishConnection}
              >
                {({ data, hasData, error }) => (
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
                              icon={
                                hasData ? (
                                  <img src={data.icon} width={24} height={24} />
                                ) : (
                                  <Loader />
                                )
                              }
                              i18nTitle={
                                hasData
                                  ? `2. ${data.connector!.name}`
                                  : '2. Finish'
                              }
                              i18nTooltip={
                                hasData ? `2. ${data.name}` : 'Finish'
                              }
                              active={true}
                              showDetails={expanded}
                              description={'Choose an action'}
                            />
                          </>
                        )}
                      </IntegrationVerticalFlow>
                    }
                    content={
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<Loader />}
                        errorChildren={<div>TODO</div>}
                      >
                        {() => (
                          <>
                            <PageHeader>
                              <Breadcrumb>
                                <Link to={resolvers.list({})}>
                                  Integrations
                                </Link>
                                <Link
                                  to={resolvers.create.start.selectConnection(
                                    {}
                                  )}
                                >
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
                                <Link
                                  to={resolvers.create.finish.selectConnection({
                                    integration,
                                    startAction,
                                    startConnection,
                                  })}
                                >
                                  Finish Connection
                                </Link>
                                <span>Choose Action</span>
                              </Breadcrumb>

                              <h1>Choose Action</h1>
                              <p>
                                Choose an action for the selected connection.
                              </p>
                            </PageHeader>
                            <div className={'container-fluid'}>
                              <ListView>
                                {data.actionsWithTo
                                  .sort((a, b) => a.name.localeCompare(b.name))
                                  .map((a, idx) => (
                                    <Link
                                      to={resolvers.create.finish.configureAction(
                                        {
                                          actionId: a.id!,
                                          finishConnection,
                                          integration,
                                          startAction,
                                          startConnection,
                                        }
                                      )}
                                      style={{
                                        color: 'inherit',
                                        textDecoration: 'none',
                                      }}
                                      key={idx}
                                    >
                                      <ListView.Item
                                        heading={a.name}
                                        description={a.description}
                                      />
                                    </Link>
                                  ))}
                              </ListView>
                            </div>
                          </>
                        )}
                      </WithLoader>
                    }
                  />
                )}
              </WithConnection>
            );
          }}
        </WithRouter>
      </WithClosedNavigation>
    );
  }
}
