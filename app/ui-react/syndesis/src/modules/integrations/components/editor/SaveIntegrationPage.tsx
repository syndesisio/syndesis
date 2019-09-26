import {
  setIntegrationProperties,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import { ErrorResponse, IntegrationSaveErrorResponse } from '@syndesis/models';
import {
  IntegrationEditorLayout,
  IntegrationSaveForm,
  SyndesisAlert,
  SyndesisAlertLevel,
} from '@syndesis/ui';
import { validateRequiredProperties, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import i18n from '../../../../i18n';
import { PageTitle } from '../../../../shared';
import {
  IWithLeaveConfirmationBaseProps,
  WithLeaveConfirmation,
} from '../../../../shared/WithLeaveConfirmation';
import {
  IPageWithEditorBreadcrumb,
  IPostPublishRouteParams,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
} from './interfaces';

export interface ISaveIntegrationForm {
  name: string;
  description?: string;
}

export interface ISaveIntegrationPageProps
  extends IWithLeaveConfirmationBaseProps,
    IPageWithEditorBreadcrumb {
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
export const SaveIntegrationPage: React.FunctionComponent<
  ISaveIntegrationPageProps
> = ({
  postPublishHref,
  postSaveHref,
  getBreadcrumb,
  cancelHref,
  ...props
}) => {
  const [error, setError] = React.useState<
    false | ErrorResponse | IntegrationSaveErrorResponse
  >(false);
  const { t } = useTranslation('shared');
  return (
    <WithLeaveConfirmation {...props}>
      {({ allowNavigation }) => (
        <UIContext.Consumer>
          {({ pushNotification }) => (
            <WithRouteData<
              ISaveIntegrationRouteParams,
              ISaveIntegrationRouteState
            >>
              {(params, state, { history }) => (
                <WithIntegrationHelpers>
                  {({ deployIntegration, saveIntegration }) => {
                    let shouldPublish = false;
                    const onSave = async (
                      { name, description }: ISaveIntegrationForm,
                      actions: any
                    ) => {
                      setError(false);
                      try {
                        const updatedIntegration = setIntegrationProperties(
                          state.integration,
                          {
                            description,
                            name,
                          }
                        );
                        const savedIntegration = await saveIntegration(
                          updatedIntegration
                        );
                        if (shouldPublish) {
                          pushNotification(
                            i18n.t('integrations:PublishingIntegrationMessage'),
                            'info'
                          );
                          try {
                            await deployIntegration(
                              savedIntegration.id!,
                              savedIntegration.version!,
                              false
                            );
                          } catch (err) {
                            pushNotification(
                              i18n.t(
                                'integrations:PublishingIntegrationFailedMessage',
                                {
                                  error: err.errorMessage || err.message || err,
                                }
                              ),
                              'warning'
                            );
                          }
                        }
                        allowNavigation();
                        if (shouldPublish) {
                          shouldPublish = false;
                          history.push(
                            postPublishHref({
                              integrationId: savedIntegration.id!,
                            })
                          );
                        } else {
                          history.push(
                            postSaveHref(
                              { integrationId: savedIntegration.id! },
                              { ...state, integration: savedIntegration }
                            )
                          );
                        }
                      } catch (err) {
                        if (Array.isArray(err)) {
                          setError(err[0]);
                        } else {
                          setError(err);
                        }
                      }
                      actions.setSubmitting(false);
                    };
                    const definition: IFormDefinition = {
                      description: {
                        defaultValue: '',
                        displayName: t('shared:Description'),
                        order: 1,
                        type: 'textarea',
                      },
                      name: {
                        defaultValue: '',
                        displayName: t('shared:Name'),
                        order: 0,
                        required: true,
                        type: 'string',
                      },
                    };
                    const validator = (values: ISaveIntegrationForm) =>
                      validateRequiredProperties(
                        definition,
                        (name: string) => `${name} is required`,
                        values
                      );
                    return (
                      <AutoForm<ISaveIntegrationForm>
                        i18nRequiredProperty={t('shared:requiredFieldMessage')}
                        definition={definition}
                        initialValue={{
                          description: state.integration.description,
                          name: state.integration.name,
                        }}
                        validate={validator}
                        validateInitial={validator}
                        onSave={onSave}
                      >
                        {({
                          fields,
                          handleSubmit,
                          isSubmitting,
                          isValid,
                          submitForm,
                        }) => (
                          <>
                            <PageTitle
                              title={t('integrations:editor:save:title')}
                            />
                            <IntegrationEditorLayout
                              title={t('integrations:editor:save:title')}
                              description={t(
                                'integrations:editor:save:description'
                              )}
                              toolbar={getBreadcrumb(
                                t('integrations:editor:save:title'),
                                params,
                                state
                              )}
                              content={
                                <IntegrationSaveForm
                                  handleSubmit={handleSubmit}
                                  onSave={submitForm}
                                  isSaveDisabled={!isValid}
                                  isSaveLoading={isSubmitting}
                                  onPublish={async () => {
                                    shouldPublish = true;
                                    await submitForm();
                                  }}
                                  isPublishDisabled={!isValid}
                                  isPublishLoading={isSubmitting}
                                  i18nSave={t('shared:Save')}
                                  i18nSaveAndPublish={t(
                                    'integrations:editor:save:saveAndPublish'
                                  )}
                                >
                                  <>
                                    {error && (
                                      <SyndesisAlert
                                        level={SyndesisAlertLevel.ERROR}
                                        message={
                                          (error as ErrorResponse).userMsg ||
                                          (error as IntegrationSaveErrorResponse)
                                            .message
                                        }
                                        detail={
                                          (error as ErrorResponse)
                                            .developerMsg ||
                                          (error as IntegrationSaveErrorResponse)
                                            .error
                                        }
                                        i18nTextExpanded={t(
                                          'shared:HideDetails'
                                        )}
                                        i18nTextCollapsed={t(
                                          'shared:ShowDetails'
                                        )}
                                      />
                                    )}
                                    {fields}
                                  </>
                                </IntegrationSaveForm>
                              }
                              cancelHref={cancelHref(params, state)}
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
      )}
    </WithLeaveConfirmation>
  );
};
