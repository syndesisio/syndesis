import { WithConnection } from '@syndesis/api';
import { Breadcrumb, Loader, PageHeader } from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { reverse } from 'named-urls';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import routes from '../routes';

export class IntegrationCreatorStartActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouter>
          {({ match }) => {
            const { connectionId } = match.params as any;
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
                            <Link
                              to={
                                routes.integrations.create.start
                                  .selectConnection
                              }
                            >
                              New integration
                            </Link>
                            <span>Start connection</span>
                          </Breadcrumb>

                          <h1>Choose Action</h1>
                          <p>Choose an action for the selected connection.</p>
                        </PageHeader>
                        <div className={'container-fluid'}>
                          <ListView>
                            {data.actionsWithFrom
                              .sort((a, b) => a.name.localeCompare(b.name))
                              .map((a, idx) => (
                                <Link
                                  to={reverse(
                                    routes.integrations.create.start
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
      </WithClosedNavigation>
    );
  }
}
