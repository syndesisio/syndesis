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
import { WithClosedNavigation } from '../../../containers';
import routes from '../routes';

export class IntegrationCreatorStartConfigurationPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouter>
          {({ match, history }) => {
            const { actionId, connectionId, step = 0 } = match.params as any;
            return (
              <WithIntegrationHelpers>
                {({
                  addConnection,
                  getEmptyIntegration,
                  updateConnection,
                  getCreationDraft,
                  setCreationDraft,
                }) => (
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
                          const integration =
                            step === 0
                              ? getEmptyIntegration()
                              : getCreationDraft();
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
                              setCreationDraft(updatedIntegration);
                              history.push(
                                reverse(
                                  routes.integrations.create.finish
                                    .configureAction,
                                  {
                                    actionId,
                                    connectionId,
                                    step: step + 1,
                                  }
                                )
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
                              setCreationDraft(updatedIntegration);
                              history.push(
                                reverse(
                                  routes.integrations.create.finish
                                    .selectConnection
                                )
                              );
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
      </WithClosedNavigation>
    );
  }
}
