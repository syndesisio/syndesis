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
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import {
  IIntegrationEditorConfigureConnectionOnSaveProps,
  IntegrationEditorConfigureConnection,
} from '../components';
import resolvers from '../resolvers';

export interface IIntegrationCreatorStartConfigurationPageRouteParams {
  actionId: string;
  connectionId: string;
  step?: number;
}

export interface IIntegrationCreatorStartConfigurationPageRouteState {
  connection: ConnectionOverview;
  integration?: Integration;
}

export class IntegrationCreatorStartConfigurationPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithIntegrationHelpers>
          {({ addConnection, getEmptyIntegration, updateConnection }) => (
            <WithRouteData<
              IIntegrationCreatorStartConfigurationPageRouteParams,
              IIntegrationCreatorStartConfigurationPageRouteState
            >>
              {(
                { actionId, connectionId, step = 0 },
                { connection, integration = getEmptyIntegration() },
                { history }
              ) => {
                const onSave = async ({
                  moreSteps,
                  configuredProperties,
                  action,
                }: IIntegrationEditorConfigureConnectionOnSaveProps) => {
                  if (moreSteps) {
                    const updatedIntegration = await updateConnection(
                      integration,
                      connection,
                      action,
                      0,
                      1,
                      configuredProperties
                    );
                    history.push(
                      resolvers.create.start.configureAction({
                        actionId,
                        connection,
                        integration: updatedIntegration,
                        step: step + 1,
                      })
                    );
                  } else {
                    const updatedIntegration = await (step === 0
                      ? addConnection
                      : updateConnection)(
                      integration,
                      connection,
                      action,
                      0,
                      0,
                      configuredProperties
                    );
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
                      <IntegrationEditorConfigureConnection
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
                          <Link
                            to={resolvers.create.start.selectAction({
                              connection,
                            })}
                            key={3}
                          >
                            Start connection
                          </Link>,
                          <span key={4}>Configure action</span>,
                        ]}
                        connection={connection}
                        actionId={actionId}
                        step={step}
                        backLink={resolvers.create.start.selectAction({
                          connection,
                        })}
                        onSave={onSave}
                      />
                    }
                  />
                );
              }}
            </WithRouteData>
          )}
        </WithIntegrationHelpers>
      </WithClosedNavigation>
    );
  }
}
