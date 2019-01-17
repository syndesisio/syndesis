import { WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
import {
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithClosedNavigation } from '../../../../../containers';
import { PageTitle } from '../../../../../containers/PageTitle';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorConfigureConnection,
  IOnUpdatedIntegrationProps,
} from '../../../components';
import resolvers from '../../../resolvers';

export interface IStartConfigurationPageRouteParams {
  actionId: string;
  connectionId: string;
  step?: string;
}

export interface IStartConfigurationPageRouteState {
  connection: ConnectionOverview;
  integration?: Integration;
}

export class StartConfigurationPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithIntegrationHelpers>
          {({ addConnection, getEmptyIntegration, updateConnection }) => (
            <WithRouteData<
              IStartConfigurationPageRouteParams,
              IStartConfigurationPageRouteState
            >>
              {(
                { actionId, connectionId, step = '0' },
                { connection, integration = getEmptyIntegration() },
                { history }
              ) => {
                const stepAsNumber = parseInt(step, 10);
                const onUpdatedIntegration = async ({
                  action,
                  moreConfigurationSteps,
                  values,
                }: IOnUpdatedIntegrationProps) => {
                  const updatedIntegration = await (stepAsNumber === 0
                    ? addConnection
                    : updateConnection)(
                    integration,
                    connection,
                    action,
                    0,
                    0,
                    values
                  );
                  if (moreConfigurationSteps) {
                    history.push(
                      resolvers.create.start.configureAction({
                        actionId,
                        connection,
                        integration: updatedIntegration,
                        step: stepAsNumber + 1,
                      })
                    );
                  } else {
                    history.push(
                      resolvers.create.finish.selectConnection({
                        integration: updatedIntegration,
                        startAction: action,
                        startConnection: connection,
                      })
                    );
                  }
                };
                return (
                  <>
                    <PageTitle title={'Configure the action'} />
                    <ContentWithSidebarLayout
                      sidebar={
                        <IntegrationVerticalFlow disabled={true}>
                          {({ expanded }) => (
                            <>
                              <IntegrationFlowStepGeneric
                                icon={
                                  <img
                                    src={connection.icon}
                                    width={24}
                                    height={24}
                                  />
                                }
                                i18nTitle={`1. ${connection.connector!.name}`}
                                i18nTooltip={`1. ${connection.name}`}
                                active={true}
                                showDetails={expanded}
                                description={'Configure the action'}
                              />
                              <IntegrationFlowStepWithOverview
                                icon={<i className={'fa fa-plus'} />}
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
                        <IntegrationEditorConfigureConnection
                          breadcrumb={
                            <IntegrationCreatorBreadcrumbs
                              step={3}
                              startConnection={connection}
                            />
                          }
                          connection={connection}
                          actionId={actionId}
                          configurationStep={stepAsNumber}
                          backLink={resolvers.create.start.selectAction({
                            connection,
                          })}
                          onUpdatedIntegration={onUpdatedIntegration}
                        />
                      }
                    />
                  </>
                );
              }}
            </WithRouteData>
          )}
        </WithIntegrationHelpers>
      </WithClosedNavigation>
    );
  }
}
