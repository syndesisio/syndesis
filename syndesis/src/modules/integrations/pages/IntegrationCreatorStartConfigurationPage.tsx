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
                          const onSave = async (configuredProperties: {
                            [key: string]: string;
                          }) => {
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
                                resolvers.create.start.configureAction({
                                  actionId,
                                  connection: data,
                                  integration: updatedIntegration,
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
                                0,
                                configuredProperties
                              );
                              history.push(
                                resolvers.create.finish.selectConnection({
                                  integration: updatedIntegration,
                                  startAction: action,
                                  startConnection: data,
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
                                        connection: data,
                                      })}
                                      key={3}
                                    >
                                      Start connection
                                    </Link>,
                                    <span key={4}>Configure action</span>,
                                  ]}
                                  definition={definition}
                                  i18nTitle={action.name}
                                  i18nSubtitle={action.description || ''}
                                  moreSteps={moreSteps}
                                  backLink={resolvers.create.start.selectAction(
                                    { connection: data }
                                  )}
                                  onSave={onSave}
                                />
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
