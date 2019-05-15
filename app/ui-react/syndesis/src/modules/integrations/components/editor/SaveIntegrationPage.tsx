import {
  setIntegrationProperties,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import { IntegrationEditorLayout, IntegrationSaveForm } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { UIContext } from '../../../../app';
import i18n from '../../../../i18n';
import { PageTitle } from '../../../../shared';
import {
  IPostPublishRouteParams,
  ISaveIntegrationForm,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
} from './interfaces';

export interface ISaveIntegrationPageProps {
  cancelHref: (
    p: ISaveIntegrationRouteParams,
    s: ISaveIntegrationRouteState
  ) => H.LocationDescriptor;
  postSaveHref: (
    p: ISaveIntegrationRouteParams,
    s: ISaveIntegrationRouteState
  ) => H.LocationDescriptorObject;
  postPublishHref: (p: IPostPublishRouteParams) => H.LocationDescriptorObject;
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
export class SaveIntegrationPage extends React.Component<
  ISaveIntegrationPageProps
> {
  public render() {
    return (
      <UIContext.Consumer>
        {({ pushNotification }) => (
          <WithRouteData<
            ISaveIntegrationRouteParams,
            ISaveIntegrationRouteState
          >>
            {({ flowId }, { integration }, { history }) => (
              <WithIntegrationHelpers>
                {({ deployIntegration, saveIntegration }) => {
                  let shouldPublish = false;
                  const onSave = async (
                    { name, description }: ISaveIntegrationForm,
                    actions: any
                  ) => {
                    const updatedIntegration = setIntegrationProperties(
                      integration,
                      {
                        description,
                        name,
                      }
                    );
                    const savedIntegration = await saveIntegration(
                      updatedIntegration
                    );
                    actions.setSubmitting(false);

                    if (shouldPublish) {
                      pushNotification(
                        i18n.t('integrations:PublishingIntegrationMessage'),
                        'info'
                      );
                      deployIntegration(
                        savedIntegration.id!,
                        savedIntegration.version!,
                        false
                      )
                        .then(() => {
                          /* nothing to do on success */
                        })
                        .catch(err => {
                          pushNotification(
                            i18n.t(
                              'integrations:PublishingIntegrationFailedMessage',
                              {
                                error: err.errorMessage || err.message || err,
                              }
                            ),
                            'warning'
                          );
                        });
                    }
                    if (shouldPublish) {
                      shouldPublish = false;
                      history.push(
                        this.props.postPublishHref({
                          integrationId: savedIntegration.id!,
                        })
                      );
                    } else {
                      history.push(
                        this.props.postSaveHref(
                          { flowId, integrationId: savedIntegration.id! },
                          { integration: savedIntegration }
                        )
                      );
                    }
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
                        <>
                          <PageTitle title={'Save the integration'} />
                          <IntegrationEditorLayout
                            title={'Save the integration'}
                            description={
                              'Update details about this integration.'
                            }
                            content={
                              <IntegrationSaveForm handleSubmit={handleSubmit}>
                                {fields}
                              </IntegrationSaveForm>
                            }
                            cancelHref={this.props.cancelHref(
                              { flowId },
                              { integration }
                            )}
                            onSave={submitForm}
                            isSaveDisabled={dirty && !isValid}
                            isSaveLoading={isSubmitting}
                            onPublish={async () => {
                              shouldPublish = true;
                              await submitForm();
                            }}
                            isPublishDisabled={dirty && !isValid}
                            isPublishLoading={isSubmitting}
                          />
                        </>
                      )}
                    </AutoForm>
                  );
                }}
              </WithIntegrationHelpers>
            )}
          </WithRouteData>
        )}
      </UIContext.Consumer>
    );
  }
}
