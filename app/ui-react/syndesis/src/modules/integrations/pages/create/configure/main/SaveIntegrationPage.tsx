import { setIntegrationName, WithIntegrationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { IntegrationEditorForm, IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../shared';
import { ISaveForm } from '../../../../../connections/pages/create/ReviewPage';
import { IntegrationCreatorBreadcrumbs } from '../../../../components';
import {
  ISaveIntegrationForm,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
} from '../../../../components/editor/interfaces';
import resolvers from '../../../../resolvers';

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
                  order: 0,
                  required: true,
                  type: 'string',
                },
                // tslint:disable-next-line
                description: {
                  defaultValue: '',
                  displayName: 'Description',
                  order: 1,
                  type: 'textarea',
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
                      header={<IntegrationCreatorBreadcrumbs step={4} />}
                      content={
                        <>
                          <PageTitle title={'Create an integration'} />
                          <IntegrationEditorForm
                            i18nTitle={'Create an integration'}
                            i18nSubtitle={'Add details about this integration.'}
                            handleSubmit={handleSubmit}
                          >
                            {fields}
                          </IntegrationEditorForm>
                        </>
                      }
                      cancelHref={resolvers.list()}
                      backHref={resolvers.create.configure.index({
                        flow,
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
