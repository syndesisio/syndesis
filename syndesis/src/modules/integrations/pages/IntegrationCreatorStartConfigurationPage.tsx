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

export class IntegrationCreatorStartConfigurationPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouter>
          {({ match, history, location }) => {
            const { actionId, connectionId, step = 0 } = match.params as any;
            return (
              <WithIntegrationHelpers>
                {({ addConnection, getEmptyIntegration, updateConnection }) => (
                  <WithConnection
                    id={connectionId}
                    initialValue={(location.state || {}).connection}
                  >
                    {({ data, hasData, error }) => (
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<Loader />}
                        errorChildren={<div>TODO</div>}
                      >
                        {() => {
                          const action = getActionById(
                            getConnectorActions(getConnectionConnector(data)),
                            actionId
                          );
                          const steps = getActionSteps(
                            getActionDescriptor(action)
                          );
                          const definition = getActionStepDefinition(
                            getActionStep(steps, step)
                          );
                          const moreSteps = step < steps.length - 1;
                          const integration =
                            step === 0
                              ? getEmptyIntegration()
                              : location.state.integration;
                          const onSave = async (configuredProperties: any) => {
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
                                  connection: data,
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
                                0,
                                configuredProperties
                              );
                              history.push({
                                pathname: reverse(
                                  routes.integrations.create.finish
                                    .selectConnection
                                ),
                                state: {
                                  integration: updatedIntegration,
                                  startAction: action,
                                  startConnection: data,
                                },
                              });
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
                                            connectionId,
                                          }
                                        )}
                                      >
                                        Start connection
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
                                        backLink={reverse(
                                          routes.integrations.create.start
                                            .selectAction,
                                          {
                                            connectionId,
                                          }
                                        )}
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
                              }
                            />
                          );
                        }}
                      </WithLoader>
                    )}
                  </WithConnection>
                )}
              </WithIntegrationHelpers>
            );
          }}
        </WithRouter>
      </WithClosedNavigation>
    );
  }
}
