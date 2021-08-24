import {
  setIntegrationProperties,
  useExtensions,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import {
  Dependency,
  ErrorResponse,
  IntegrationSaveErrorResponse,
} from '@syndesis/models';
import {
  IntegrationEditorExtensionTable,
  IntegrationEditorLabels,
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

interface IExtensionProps {
  description: string;
  extensionId: string;
  lastUpdated: number;
  name: string;
  selected?: boolean;
}

export interface ISaveIntegrationForm {
  name: string;
  description?: string;
  /**
   * Array of dependencies or
   * extensions for this integration
   */
  dependencies?: Dependency[];
  labels?: { [key: string]: string };
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

const convertLabelObjectToArray = (labels: { [s: string]: string }) => {
  const initialArray: string[] = [];

  Object.entries(labels).map((l, k) => {
    initialArray.push(l[0] + '=' + l[1]);
  });

  return initialArray;
};

const convertLabelArrayIntoObject = (
  labels: string[]
): { [s: string]: string } => {
  let newObj: { [s: string]: string } = {};

  labels.map((label) => {
    const splitLabel = label.split('=');
    const labelKey = splitLabel[0];
    newObj = { ...newObj, [labelKey]: splitLabel[1] };
  });
  return newObj;
};

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
export const SaveIntegrationPage: React.FunctionComponent<ISaveIntegrationPageProps> =
  ({ postPublishHref, postSaveHref, getBreadcrumb, cancelHref, ...props }) => {
    const { resource: extensionsData } = useExtensions(undefined, 'Libraries');

    const [currentSelectedExtensionIds, setSelectedExtensionIds] =
      React.useState<string[]>([]);
    const [currentSelectLabels, setCurrentSelectLabels] = React.useState<{
      [key: string]: string;
    }>({});
    const [error, setError] = React.useState<
      false | ErrorResponse | IntegrationSaveErrorResponse
    >(false);

    const { t } = useTranslation('shared');

    const extensions: IExtensionProps[] = extensionsData.items as [];
    const dependencyList = React.useRef<Dependency[]>([]);
    const labelList = React.useRef<{ [key: string]: string }>({});

    /**
     * Here we will return an array of extension IDs later
     * to be provided by the API
     */
    const preSelectedExtensions: string[] = currentSelectedExtensionIds || [];

    /**
     * Updates the state based on changes in the UI
     */
    const onSelectExtensions = React.useCallback(
      (extensionIds: string[]) => {
        setSelectedExtensionIds(extensionIds);

        dependencyList.current = extensionIds.map((extension: string) => {
          return {
            id: extension,
            type: 'EXTENSION_TAG',
          };
        });
      },
      [dependencyList]
    );

    const onSelectLabels = React.useCallback(
      (labels: string[]) => {
        // parse into key/value pairs

        labelList.current = convertLabelArrayIntoObject(labels);
      },
      [labelList]
    );

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
                        {
                          name,
                          description,
                          dependencies,
                          labels,
                        }: ISaveIntegrationForm,
                        actions: any
                      ) => {
                        setError(false);

                        try {
                          const updatedIntegration = setIntegrationProperties(
                            state.integration,
                            {
                              dependencies: dependencyList.current,
                              description,
                              labels: currentSelectLabels.current,
                              name,
                            }
                          );

                          const savedIntegration = await saveIntegration(
                            updatedIntegration
                          );
                          if (shouldPublish) {
                            pushNotification(
                              i18n.t(
                                'integrations:PublishingIntegrationMessage'
                              ),
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
                                    error:
                                      err.errorMessage || err.message || err,
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
                        <>
                          <AutoForm<ISaveIntegrationForm>
                            i18nRequiredProperty={t(
                              'shared:requiredFieldMessage'
                            )}
                            definition={definition}
                            initialValue={{
                              dependencies: dependencyList.current,
                              description: state.integration.description,
                              labels: convertLabelObjectToArray(
                                currentSelectLabels.current
                              ),
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
                                              (error as ErrorResponse)
                                                .userMsg ||
                                              (
                                                error as IntegrationSaveErrorResponse
                                              ).message
                                            }
                                            detail={
                                              (error as ErrorResponse)
                                                .developerMsg ||
                                              (
                                                error as IntegrationSaveErrorResponse
                                              ).error
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
                                        <IntegrationEditorLabels
                                          labels={currentSelectLabels.current}
                                        />
                                        {extensions.length > 0 && (
                                          <IntegrationEditorExtensionTable
                                            extensionsAvailable={extensions}
                                            i18nHeaderDescription={t(
                                              'integrations:editor:extensions:description'
                                            )}
                                            i18nHeaderLastUpdated={t(
                                              'integrations:editor:extensions:lastUpdated'
                                            )}
                                            i18nHeaderName={t(
                                              'integrations:editor:extensions:name'
                                            )}
                                            i18nTableDescription={t(
                                              'integrations:editor:extensions:tableDescription'
                                            )}
                                            i18nTableName={t(
                                              'integrations:editor:extensions:tableName'
                                            )}
                                            onSelectExtensions={
                                              onSelectExtensions
                                            }
                                            preSelectedExtensionIds={
                                              preSelectedExtensions
                                            }
                                          />
                                        )}
                                      </>
                                    </IntegrationSaveForm>
                                  }
                                  cancelHref={cancelHref(params, state)}
                                />
                              </>
                            )}
                          </AutoForm>
                        </>
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
