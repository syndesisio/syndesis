import { WithConnection } from '@syndesis/api';
import { Action, ConnectionOverview } from '@syndesis/models';
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
import { reverse } from 'named-urls';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import routes from '../routes';

export class IntegrationCreatorFinishActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouter>
          {({ match, location }) => {
            const { connectionId } = match.params as any;
            const startConnection: ConnectionOverview =
              location.state.startConnection;
            const startAction: Action = location.state.startAction;
            return (
              <WithConnection
                id={(match.params as any).connectionId}
                initialValue={location.state.finishConnection}
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
                                <Link to={routes.integrations.list}>
                                  Integrations
                                </Link>
                                <Link
                                  to={
                                    routes.integrations.create.start
                                      .selectConnection
                                  }
                                >
                                  New integration
                                </Link>
                                <Link
                                  to={reverse(
                                    routes.integrations.create.start
                                      .selectAction,
                                    {
                                      connectionId: startConnection.id,
                                    }
                                  )}
                                >
                                  Start connection
                                </Link>
                                <Link
                                  to={reverse(
                                    routes.integrations.create.start
                                      .configureAction,
                                    {
                                      actionId: startAction.id,
                                      connectionId: startConnection.id,
                                    }
                                  )}
                                >
                                  Configure action
                                </Link>
                                <Link
                                  to={reverse(
                                    routes.integrations.create.finish
                                      .selectConnection
                                  )}
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
                                      to={{
                                        pathname: reverse(
                                          routes.integrations.create.finish
                                            .configureAction,
                                          {
                                            actionId: a.id,
                                            connectionId,
                                          }
                                        ),
                                        state: {
                                          ...location.state,
                                        },
                                      }}
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
