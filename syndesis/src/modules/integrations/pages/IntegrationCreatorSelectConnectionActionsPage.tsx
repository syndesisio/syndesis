import { WithConnection } from '@syndesis/api';
import { Breadcrumb, Loader, PageHeader } from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export class IntegrationCreatorSelectConnectionActionsPage extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
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
                        <Link to={'/'}>Home</Link>
                        <Link to={'/integrations'}>Integrations</Link>
                        <Link to={'/integrations/create'}>New integration</Link>
                        <span>{data.name}</span>
                      </Breadcrumb>

                      <h1>Choose Action</h1>
                      <p>Choose an action for the selected connection.</p>
                    </PageHeader>
                    <div className={'container-fluid'}>
                      <ListView>
                        {data.actionsWithFrom.map((a, idx) => (
                          <Link
                            to={`${match.url}/${a.id}`}
                            style={{ color: 'inherit', textDecoration: 'none' }}
                          >
                            <ListView.Item
                              heading={a.name}
                              description={a.description}
                              key={idx}
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
        )}
      </WithRouter>
    );
  }
}
