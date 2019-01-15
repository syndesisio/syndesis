import { WithConnection } from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
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
  startConnection: ConnectionOverview,
  startAction: Action,
  finishConnection: ConnectionOverview,
  integration: Integration,
  action: Action
): H.LocationDescriptor {
  return resolvers.create.finish.configureAction({
    actionId: action.id!,
    finishConnection,
    integration,
    startAction,
    startConnection,
  });
}

export interface IIntegrationCreatorFinishActionRouteParams {
  connectionId: string;
}

export interface IIntegrationCreatorFinishActionRouteState {
  startConnection: ConnectionOverview;
  startAction: Action;
  integration: Integration;
  finishConnection: ConnectionOverview;
}

export class IntegrationCreatorFinishActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<
          IIntegrationCreatorFinishActionRouteParams,
          IIntegrationCreatorFinishActionRouteState
        >>
          {(
            { connectionId },
            { startConnection, startAction, integration, finishConnection }
          ) => (
            <WithConnection id={connectionId} initialValue={finishConnection}>
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
                            i18nTooltip={hasData ? `2. ${data.name}` : 'Finish'}
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
                        <IntegrationEditorChooseAction
                          breadcrumb={
                            <IntegrationCreatorBreadcrumbs
                              step={5}
                              startConnection={startConnection}
                              startAction={startAction}
                              integration={integration}
                            />
                          }
                          actions={data.actionsWithTo.sort((a, b) =>
                            a.name.localeCompare(b.name)
                          )}
                          getActionHref={getActionHref.bind(
                            null,
                            startConnection,
                            startAction,
                            finishConnection,
                            integration
                          )}
                        />
                      )}
                    </WithLoader>
                  }
                />
              )}
            </WithConnection>
          )}
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
