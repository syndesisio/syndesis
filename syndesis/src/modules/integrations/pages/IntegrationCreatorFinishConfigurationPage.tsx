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
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Action, ConnectionOverview } from '@syndesis/models';
import {
  Breadcrumb,
  ContentWithSidebarLayout,
  IntegrationActionConfigurationForm,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
  PageHeader,
} from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { reverse } from 'named-urls';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import routes from '../routes';

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
                return (
                  <WithConnection
                    id={connectionId}
                    initialValue={location.state.finishConnection}
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
                              const integration = location.state.integration;
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
                                  history.push({
                                    pathname: reverse(
                                      routes.integrations.create.finish
                                        .configureAction,
                                      {
                                        actionId,
                                        connectionId,
                                        step: step + 1,
                                      }
                                    ),
                                    state: {
                                      ...location.state,
                                      integration: updatedIntegration,
                                    },
                                  });
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
                                  history.push({
                                    pathname: reverse(
                                      routes.integrations.create.configure.index
                                    ),
                                    state: {
                                      integration: updatedIntegration,
                                    },
                                  });
                                }
                              };
                              return (
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
                                      <Link
                                        to={reverse(
                                          routes.integrations.create.start
                                            .selectAction,
                                          {
                                            connectionId: startConnection.id,
                                          }
                                        )}
                                      >
                                        Start connection
                                      </Link>
                                      <Link
                                        to={reverse(
                                          routes.integrations.create.start
                                            .configureAction,
                                          {
                                            actionId: startAction.id,
                                            connectionId: startConnection.id,
                                          }
                                        )}
                                      >
                                        Configure action
                                      </Link>
                                      <Link
                                        to={reverse(
                                          routes.integrations.create.finish
                                            .selectConnection
                                        )}
                                      >
                                        Finish Connection
                                      </Link>
                                      <Link
                                        to={reverse(
                                          routes.integrations.create.finish
                                            .selectAction,
                                          {
                                            connectionId,
                                          }
                                        )}
                                      >
                                        Choose Action
                                      </Link>
                                      <span>Configure action</span>
                                    </Breadcrumb>

                                    <h1>{action.name}</h1>
                                    <p>{action.description}</p>
                                  </PageHeader>
                                  <AutoForm
                                    i18nRequiredProperty={'* Required field'}
                                    definition={definition as IFormDefinition}
                                    initialValue={{}}
                                    onSave={onSave}
                                  >
                                    {({ fields, handleSubmit }) => (
                                      <IntegrationActionConfigurationForm
                                        backLink={`/integrations/create/${
                                          data.id
                                        }`}
                                        fields={fields}
                                        handleSubmit={handleSubmit}
                                        i18nBackLabel={'< Choose action'}
                                        i18nSubmitLabel={
                                          moreSteps ? 'Continue' : 'Done'
                                        }
                                      />
                                    )}
                                  </AutoForm>
                                </>
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
