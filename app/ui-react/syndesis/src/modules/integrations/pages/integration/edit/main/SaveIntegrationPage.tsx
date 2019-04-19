import { setIntegrationName, WithIntegrationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { OptionalInt } from '@syndesis/models';
import { IntegrationEditorForm, IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../shared';
import { IntegrationEditorBreadcrumbs } from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  ISaveIntegrationForm,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
} from '../../../editorInterfaces';

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
      <WithRouteData<ISaveIntegrationRouteParams, ISaveIntegrationRouteState>>
        {({ flow }, { integration }, { history }) => (
          <WithIntegrationHelpers>
            {({ saveIntegration }) => {
              const onSave = async (
                { name, description }: ISaveIntegrationForm,
                actions: any
              ) => {
                const updatedIntegration = setIntegrationName(
                  integration,
                  name
                );
                // TODO: set the description
                await saveIntegration(updatedIntegration);
                actions.setSubmitting(false);
                // TODO: toast notification
                history.push(resolvers.list());
              };
              const definition: IFormDefinition = {
                name: {
                  defaultValue: '',
                  displayName: 'Name',
                  order: 0 as OptionalInt,
                  required: true,
                },
                // tslint:disable-next-line
                description: {
                  defaultValue: '',
                  displayName: 'Description',
                  order: 1 as OptionalInt,
                  type: 'textarea',
                },
              };
              return (
                <AutoForm<ISaveIntegrationForm>
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
                    dirty,
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
                          <IntegrationEditorForm
                            i18nTitle={'Save the integration'}
                            i18nSubtitle={
                              'Update details about this integration.'
                            }
                            handleSubmit={handleSubmit}
                          >
                            {fields}
                          </IntegrationEditorForm>
                        </>
                      }
                      cancelHref={resolvers.list()}
                      backHref={resolvers.integration.edit.index({
                        flow,
                        integration,
                      })}
                      onNext={submitForm}
                      isNextDisabled={dirty && !isValid}
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
