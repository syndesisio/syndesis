import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import {
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithClosedNavigation } from '../../../containers';
import { IntegrationEditorConfigureConnection } from '../components';
import { IntegrationCreatorBreadcrumbs } from '../components/IntegrationCreatorBreadcrumbs';
import { IWithAutoFormHelperOnUpdatedIntegrationProps } from '../containers';
import resolvers from '../resolvers';

export interface IIntegrationCreatorFinishConfigurationPageRouteParams {
  actionId: string;
  connectionId: string;
  step?: string;
}

export interface IIntegrationCreatorFinishConfigurationPageRouteState {
  startAction: Action;
  startConnection: ConnectionOverview;
  finishConnection: ConnectionOverview;
  integration: Integration;
}

export class IntegrationCreatorFinishConfigurationPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<
          IIntegrationCreatorFinishConfigurationPageRouteParams,
          IIntegrationCreatorFinishConfigurationPageRouteState
        >>
          {(
            { actionId, connectionId, step = '0' },
            { startAction, startConnection, finishConnection, integration },
            { history }
          ) => {
            const stepAsNumber = parseInt(step, 10);
            const onUpdatedIntegration = ({
              updatedIntegration,
              moreConfigurationSteps,
            }: IWithAutoFormHelperOnUpdatedIntegrationProps) => {
              if (moreConfigurationSteps) {
                history.push(
                  resolvers.create.finish.configureAction({
                    actionId,
                    finishConnection,
                    integration: updatedIntegration,
                    startAction,
                    startConnection,
                    step: stepAsNumber + 1,
                  })
                );
              } else {
                history.push(
                  resolvers.create.configure.index({
                    integration: updatedIntegration,
                  })
                );
              }
            };
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
                          icon={
                            <img
                              src={finishConnection.icon}
                              width={24}
                              height={24}
                            />
                          }
                          i18nTitle={`${finishConnection.connector!.name}`}
                          i18nTooltip={`2. ${finishConnection.name}`}
                          active={true}
                          showDetails={expanded}
                          description={'Configure the action'}
                        />
                      </>
                    )}
                  </IntegrationVerticalFlow>
                }
                content={
                  <IntegrationEditorConfigureConnection
                    breadcrumb={
                      <IntegrationCreatorBreadcrumbs
                        step={6}
                        startConnection={startConnection}
                        startAction={startAction}
                        finishConnection={finishConnection}
                        integration={integration}
                      />
                    }
                    integration={integration}
                    connection={finishConnection}
                    actionId={actionId}
                    configurationStep={stepAsNumber}
                    flow={0}
                    flowStep={1}
                    backLink={resolvers.create.finish.selectAction({
                      finishConnection,
                      integration,
                      startAction,
                      startConnection,
                    })}
                    onUpdatedIntegration={onUpdatedIntegration}
                  />
                }
              />
            );
          }}
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
