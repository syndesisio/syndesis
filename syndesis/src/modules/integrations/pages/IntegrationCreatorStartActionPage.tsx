import { WithConnection } from '@syndesis/api';
import { Action, ConnectionOverview } from '@syndesis/models';
import {
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { WithClosedNavigation } from '../../../containers';
import { IntegrationEditorChooseAction } from '../components';
import { IntegrationCreatorBreadcrumbs } from '../components/IntegrationCreatorBreadcrumbs';
import resolvers from '../resolvers';

function getActionHref(
  connection: ConnectionOverview,
  action: Action
): H.LocationDescriptor {
  return resolvers.create.start.configureAction({
    actionId: action.id!,
    connection,
  });
}

export interface IIntegrationCreatorStartActionRouteParams {
  connectionId: string;
}

export interface IIntegrationCreatorStartActionRouteState {
  connection?: ConnectionOverview;
}

export class IntegrationCreatorStartActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<
          IIntegrationCreatorStartActionRouteParams,
          IIntegrationCreatorStartActionRouteState
        >>
          {({ connectionId }, { connection } = {}) => {
            return (
              <WithConnection id={connectionId} initialValue={connection}>
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
                            breadcrumb={
                              <IntegrationCreatorBreadcrumbs step={2} />
                            }
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
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
