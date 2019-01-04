import { WithConnection, WithIntegrationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import {
  Breadcrumb,
  IntegrationActionConfigurationForm,
  Loader,
  PageHeader,
} from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { reverse } from 'named-urls';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { deserializeIntegration } from '../helpers';
import routes from '../routes';

export class IntegrationCreatorFinishConfigurationPage extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match, history }) => {
          const {
            actionId,
            connectionId,
            integrationData,
            step = 0,
          } = match.params as any;
          const integration = deserializeIntegration(integrationData);
          return (
            <WithIntegrationHelpers>
              {({ addConnection, saveIntegration, setName }) => (
                <WithConnection id={connectionId}>
                  {({ data, hasData, error }) => (
                    <WithLoader
                      error={error}
                      loading={!hasData}
                      loaderChildren={<Loader />}
                      errorChildren={<div>TODO</div>}
                    >
                      {() => {
                        const action = data.getActionById(actionId);
                        const steps = data.getActionSteps(action);
                        const definition = data.getActionStepDefinition(
                          data.getActionStep(action, step)
                        );
                        const moreSteps = step < steps.length - 1;
                        const onSave = async (configuredProperties: any) => {
                          console.log('onSave', configuredProperties);
                          if (moreSteps) {
                            // TODO: preserve configuration state
                            history.push(
                              reverse(match.path, {
                                ...match.params,
                                step: step + 1,
                              })
                            );
                          } else {
                            let updatedIntegration = await addConnection(
                              integration,
                              data,
                              action,
                              0,
                              1,
                              configuredProperties
                            );
                            console.log(
                              'After addConnection',
                              updatedIntegration
                            );
                            updatedIntegration = setName(
                              updatedIntegration,
                              'Hello from React!'
                            );
                            const savedIntegration = saveIntegration(
                              updatedIntegration
                            );
                            console.log('After save', savedIntegration);
                            history.push(reverse(routes.integrations.list));
                          }
                        };
                        return (
                          <>
                            <PageHeader>
                              <Breadcrumb>
                                <Link to={routes.integrations.list}>
                                  Integrations
                                </Link>
                                <Link to={routes.integrations.create.begin}>
                                  New integration
                                </Link>
                                <Link
                                  to={reverse(
                                    routes.integrations.create.start
                                      .selectAction,
                                    {
                                      connectionId: integration.flows![0]
                                        .steps![0].connection!.id,
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
                                      actionId: integration.flows![0].steps![0]
                                        .action!.id,
                                      connectionId: integration.flows![0]
                                        .steps![0].connection!.id,
                                    }
                                  )}
                                >
                                  Configure action
                                </Link>
                                <Link
                                  to={reverse(
                                    routes.integrations.create.finish
                                      .selectConnection,
                                    {
                                      integrationData,
                                    }
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
                                      integrationData,
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
                                  backLink={`/integrations/create/${data.id}`}
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
                  )}
                </WithConnection>
              )}
            </WithIntegrationHelpers>
          );
        }}
      </WithRouter>
    );
  }
}
