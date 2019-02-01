/* tslint:disable:object-literal-sort-key */
import { WithIntegrationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../containers/PageTitle';
import { IntegrationCreatorBreadcrumbs } from '../../../components';
import resolvers from '../../../resolvers';

export interface ISaveForm {
  name: string;
  description?: string;
}

export interface ISaveIntegrationRouteState {
  integration: Integration;
}

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
                await saveIntegration(updatedIntegration);
                actions.setSubmitting(false);
                // TODO: toast notification
                history.push(resolvers.list());
              };
              const definition: IFormDefinition = {
                name: {
                  defaultValue: '',
                  displayName: 'Name',
                  required: true,
                },
                description: {
                  defaultValue: '',
                  displayName: 'Description',
                  type: 'textarea',
                },
              };
              return (
                <AutoForm<ISaveForm>
                  i18nRequiredProperty={'* Required field'}
                  definition={definition}
                  initialValue={{
                    name: integration.name,
                    description: integration.description,
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
                      header={<IntegrationCreatorBreadcrumbs step={4} />}
                      content={
                        <>
                          <PageTitle title={'Create an integration'} />
                          <div className={'container-fluid'}>
                            <h1>Create an integration</h1>
                            <p>Add details about this integration.</p>
                            <form
                              className="form-horizontal required-pf"
                              role="form"
                              onSubmit={handleSubmit}
                            >
                              <div className="row row-cards-pf">
                                <div className="card-pf">
                                  <div className="card-pf-body">
                                    <div className="container-fluid">
                                      {fields}
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </form>
                          </div>
                        </>
                      }
                      cancelHref={resolvers.list()}
                      backHref={resolvers.create.configure.index({
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
