import { WithIntegrationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Integration } from '@syndesis/models';
import { Container, IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../containers/PageTitle';
import { IntegrationEditorBreadcrumbs } from '../../../../components';
import resolvers from '../../../../resolvers';

export interface ISaveForm {
  name: string;
  description?: string;
}

/**
 * @param integration - the integration object.
 */
export interface ISaveIntegrationRouteState {
  integration: Integration;
}

/**
 * This page asks for the details of the integration, and saves it.
 *
 * This component expects a [state]{@link ISaveIntegrationRouteState} to be
 * properly set in the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo toast notifications.
 * @todo redirect to the integration detail page once available.
 */
export class SaveIntegrationPage extends React.Component {
  public render() {
    return (
      <WithRouteData<null, ISaveIntegrationRouteState>>
        {(_, { integration }, { history }) => (
          <WithIntegrationHelpers>
            {({ setName, saveIntegration }) => {
              const onSave = async (
                { name, description }: ISaveForm,
                actions: any
              ) => {
                const updatedIntegration = setName(integration, name);
                // TODO: set the description
                await saveIntegration(updatedIntegration);
                actions.setSubmitting(false);
                // TODO: toast notification
                history.push(resolvers.list());
              };
              const definition: IFormDefinition = {
                description: {
                  defaultValue: '',
                  displayName: 'Description',
                  type: 'textarea',
                },
                name: {
                  defaultValue: '',
                  displayName: 'Name',
                  required: true,
                },
              };
              return (
                <AutoForm<ISaveForm>
                  i18nRequiredProperty={'* Required field'}
                  definition={definition}
                  initialValue={{
                    description: integration.description,
                    name: integration.name,
                  }}
                  onSave={onSave}
                >
                  {({
                    fields,
                    handleSubmit,
                    isSubmitting,
                    isValid,
                    submitForm,
                  }) => (
                    <IntegrationEditorLayout
                      header={<IntegrationEditorBreadcrumbs step={2} />}
                      content={
                        <>
                          <PageTitle title={'Save the integration'} />
                          <Container>
                            <h1>Save the integration</h1>
                            <p>Update details about this integration.</p>
                            <form
                              className="form-horizontal required-pf"
                              role="form"
                              onSubmit={handleSubmit}
                            >
                              <div className="row row-cards-pf">
                                <div className="card-pf">
                                  <div className="card-pf-body">
                                    <Container>{fields}</Container>
                                  </div>
                                </div>
                              </div>
                            </form>
                          </Container>
                        </>
                      }
                      cancelHref={resolvers.list()}
                      backHref={resolvers.integration.edit.index({
                        integration,
                      })}
                      onNext={submitForm}
                      isNextDisabled={!isValid}
                      isNextLoading={isSubmitting}
                      isLastStep={true}
                    />
                  )}
                </AutoForm>
              );
            }}
          </WithIntegrationHelpers>
        )}
      </WithRouteData>
    );
  }
}
