import { WithConnection } from '@syndesis/api';
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

export class IntegrationCreatorStartActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouter>
          {({ match, location }) => {
            return (
              <WithConnection
                id={(match.params as any).connectionId}
                initialValue={(location.state || {}).connection}
              >
                {({ data, hasData, error }) => (
                  <WithLoader
                    error={error}
                    loading={!hasData}
                    loaderChildren={<Loader />}
                    errorChildren={<div>TODO</div>}
                  >
                    {() => (
                      <ContentWithSidebarLayout
                        sidebar={
                          <IntegrationVerticalFlow disabled={true}>
                            {({ expanded }) => (
                              <>
                                <IntegrationFlowStepGeneric
                                  icon={
                                    hasData ? (
                                      <img
                                        src={data.icon}
                                        width={24}
                                        height={24}
                                      />
                                    ) : (
                                      <Loader />
                                    )
                                  }
                                  i18nTitle={
                                    hasData
                                      ? `1. ${data.connector!.name}`
                                      : '1. Start'
                                  }
                                  i18nTooltip={
                                    hasData ? `1. ${data.name}` : 'Start'
                                  }
                                  active={true}
                                  showDetails={expanded}
                                  description={'Choose an action'}
                                />
                                <IntegrationFlowStepWithOverview
                                  icon={'+'}
                                  i18nTitle={'2. Finish'}
                                  i18nTooltip={'Finish'}
                                  active={false}
                                  showDetails={expanded}
                                  name={'n/a'}
                                  action={'n/a'}
                                  dataType={'n/a'}
                                />
                              </>
                            )}
                          </IntegrationVerticalFlow>
                        }
                        content={
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
                                <span>Start connection</span>
                              </Breadcrumb>

                              <h1>Choose Action</h1>
                              <p>
                                Choose an action for the selected connection.
                              </p>
                            </PageHeader>
                            <div className={'container-fluid'}>
                              <ListView>
                                {data.actionsWithFrom
                                  .sort((a, b) => a.name.localeCompare(b.name))
                                  .map((a, idx) => (
                                    <Link
                                      to={resolvers.create.start.configureAction(
                                        {
                                          actionId: a.id!,
                                          connection: data,
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
                        }
                      />
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
