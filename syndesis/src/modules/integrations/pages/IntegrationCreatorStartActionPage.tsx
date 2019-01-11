import { WithConnection } from '@syndesis/api';
import { Action, Connection } from '@syndesis/models';
import {
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import { IntegrationEditorChooseAction } from '../components';
import resolvers from '../resolvers';

function getActionHref(
  connection: Connection,
  action: Action
): H.LocationDescriptor {
  return resolvers.create.start.configureAction({
    actionId: action.id!,
    connection,
  });
}

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
                          <IntegrationEditorChooseAction
                            breadcrumb={[
                              <Link to={resolvers.list()} key={1}>
                                Integrations
                              </Link>,
                              <Link
                                to={resolvers.create.start.selectConnection()}
                                key={2}
                              >
                                New integration
                              </Link>,
                              <span key={3}>Start connection</span>,
                            ]}
                            actions={data.actionsWithFrom.sort((a, b) =>
                              a.name.localeCompare(b.name)
                            )}
                            getActionHref={getActionHref.bind(null, data)}
                          />
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
