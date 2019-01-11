import {
  getActionById,
  getActionDescriptor,
  getActionStep,
  getActionStepDefinition,
  getActionSteps,
  getConnectionConnector,
  getConnectorActions,
  WithConnection,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { Action, ConnectionOverview, Integration } from '@syndesis/models';
import {
  ContentWithSidebarLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import { IntegrationEditorConfigureConnection } from '../components';
import resolvers from '../resolvers';

export class IntegrationCreatorFinishConfigurationPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithIntegrationHelpers>
          {({ addConnection, saveIntegration, updateConnection }) => (
            <WithRouter>
              {({ match, history, location }) => {
                const {
                  actionId,
                  connectionId,
                  step = 0,
                } = match.params as any;
                const startConnection: ConnectionOverview =
                  location.state.startConnection;
                const startAction: Action = location.state.startAction;
                const integration: Integration = location.state.integration;
                const finishConnection: ConnectionOverview =
                  location.state.finishConnection;
                return (
                  <WithConnection
                    id={connectionId}
                    initialValue={finishConnection}
                  >
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
                                      ? `2. ${data.connector!.name}`
                                      : '2. Start'
                                  }
                                  i18nTooltip={
                                    hasData ? `2. ${data.name}` : 'Start'
                                  }
                                  active={true}
                                  showDetails={expanded}
                                  description={'Configure the action'}
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
                            {() => {
                              const action = getActionById(
                                getConnectorActions(
                                  getConnectionConnector(data)
                                ),
                                actionId
                              );
                              const steps = getActionSteps(
                                getActionDescriptor(action)
                              );
                              const definition = getActionStepDefinition(
                                getActionStep(steps, step)
                              );
                              const moreSteps = step < steps.length - 1;
                              const onSave = async (
                                configuredProperties: any
                              ) => {
                                if (moreSteps) {
                                  const updatedIntegration = await updateConnection(
                                    integration,
                                    data,
                                    action,
                                    0,
                                    1,
                                    configuredProperties
                                  );
                                  history.push(
                                    resolvers.create.finish.configureAction({
                                      actionId,
                                      finishConnection,
                                      integration: updatedIntegration,
                                      startAction,
                                      startConnection,
                                      step: step + 1,
                                    })
                                  );
                                } else {
                                  const updatedIntegration = await (step === 0
                                    ? addConnection
                                    : updateConnection)(
                                    integration,
                                    data,
                                    action,
                                    0,
                                    1,
                                    configuredProperties
                                  );
                                  history.push(
                                    resolvers.create.configure.index({
                                      integration: updatedIntegration,
                                    })
                                  );
                                }
                              };
                              return (
                                <IntegrationEditorConfigureConnection
                                  breadcrumb={[
                                    <Link to={resolvers.list({})} key={1}>
                                      Integrations
                                    </Link>,
                                    <Link
                                      to={resolvers.create.start.selectConnection(
                                        {}
                                      )}
                                      key={2}
                                    >
                                      New integration
                                    </Link>,
                                    <Link
                                      to={resolvers.create.start.selectAction({
                                        connection: startConnection,
                                      })}
                                      key={3}
                                    >
                                      Start connection
                                    </Link>,
                                    <Link
                                      to={resolvers.create.start.configureAction(
                                        {
                                          actionId: startAction.id!,
                                          connection: startConnection,
                                        }
                                      )}
                                      key={4}
                                    >
                                      Configure action
                                    </Link>,
                                    <Link
                                      to={resolvers.create.finish.selectConnection(
                                        {
                                          integration,
                                          startAction,
                                          startConnection,
                                        }
                                      )}
                                      key={5}
                                    >
                                      Finish Connection
                                    </Link>,
                                    <Link
                                      to={resolvers.create.finish.selectAction({
                                        finishConnection,
                                        integration,
                                        startAction,
                                        startConnection,
                                      })}
                                      key={6}
                                    >
                                      Choose Action
                                    </Link>,
                                    <span key={7}>Configure action</span>,
                                  ]}
                                  definition={definition}
                                  i18nTitle={action.name}
                                  i18nSubtitle={action.description || ''}
                                  moreSteps={moreSteps}
                                  backLink={resolvers.create.finish.selectAction(
                                    {
                                      finishConnection,
                                      integration,
                                      startAction,
                                      startConnection,
                                    }
                                  )}
                                  onSave={onSave}
                                />
                              );
                            }}
                          </WithLoader>
                        }
                      />
                    )}
                  </WithConnection>
                );
              }}
            </WithRouter>
          )}
        </WithIntegrationHelpers>
      </WithClosedNavigation>
    );
  }
}
