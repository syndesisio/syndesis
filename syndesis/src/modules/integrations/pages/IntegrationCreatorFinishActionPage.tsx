import { WithConnection, WithIntegrationHelpers } from '@syndesis/api';
import { Breadcrumb, Loader, PageHeader } from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { reverse } from 'named-urls';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import routes from '../routes';

export class IntegrationCreatorFinishActionPage extends React.Component {
  public render() {
    return (
      <WithIntegrationHelpers>
        {({ getCreationDraft }) => (
          <WithRouter>
            {({ match }) => {
              const { connectionId } = match.params as any;
              const integration = getCreationDraft();
              return (
                <WithConnection id={(match.params as any).connectionId}>
                  {({ data, hasData, error }) => (
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
                              <Link to={routes.integrations.create.begin}>
                                New integration
                              </Link>
                              <Link
                                to={reverse(
                                  routes.integrations.create.start.selectAction,
                                  {
                                    connectionId: integration.flows![0]
                                      .steps![0].connection!.id,
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
                                    actionId: integration.flows![0].steps![0]
                                      .action!.id,
                                    connectionId: integration.flows![0]
                                      .steps![0].connection!.id,
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
                            <p>Choose an action for the selected connection.</p>
                          </PageHeader>
                          <div className={'container-fluid'}>
                            <ListView>
                              {data.actionsWithTo
                                .sort((a, b) => a.name.localeCompare(b.name))
                                .map((a, idx) => (
                                  <Link
                                    to={reverse(
                                      routes.integrations.create.finish
                                        .configureAction,
                                      {
                                        actionId: a.id,
                                        connectionId,
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
                  )}
                </WithConnection>
              );
            }}
          </WithRouter>
        )}
      </WithIntegrationHelpers>
    );
  }
}
