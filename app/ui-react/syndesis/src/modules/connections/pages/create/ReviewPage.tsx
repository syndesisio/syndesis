import { useConnectionHelpers, useConnector } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import { IConnector } from '@syndesis/models';
import {
  ConnectionCreatorBreadcrumb,
  ConnectionCreatorBreadSteps,
  ConnectionCreatorFooter,
  ConnectionCreatorLayout,
  ConnectionCreatorToggleList,
  ConnectorConfigurationForm,
  PageLoader,
} from '@syndesis/ui';
import {
  useRouteData,
  validateRequiredProperties,
  WithLoader,
} from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface ISaveForm {
  name: string;
  description?: string;
}

export interface IReviewPageRouteParams {
  connectorId: string;
}

export interface IReviewPageRouteState {
  connector: IConnector;
  configuredProperties?: { [key: string]: string };
}

export const ReviewPage: React.FunctionComponent = () => {
  const { t } = useTranslation(['connections', 'shared']);
  const { params, state, history } = useRouteData<
    IReviewPageRouteParams,
    IReviewPageRouteState
  >();
  const { hasData, error, resource: connector } = useConnector(
    params.connectorId,
    state.connector
  );
  const { pushNotification } = React.useContext(UIContext);
  const {
    createConnection,
    saveConnection,
    validateName,
  } = useConnectionHelpers();

  const definition: IFormDefinition = {
    name: {
      defaultValue: '',
      displayName: t('shared:Name'),
      required: true,
      type: 'string',
    },
    /* tslint:disable-next-line:object-literal-sort-keys */
    description: {
      defaultValue: '',
      displayName: t('shared:Description'),
      type: 'textarea',
    },
  };

  return (
    <WithLeaveConfirmation
      i18nTitle={t('connections:create:unsavedChangesTitle')}
      i18nConfirmationMessage={t('connections:create:unsavedChangesMessage')}
      shouldDisplayDialog={(location: H.LocationDescriptor) => {
        const url =
          typeof location === 'string' ? location : location.pathname!;
        return !url.startsWith(routes.create.root);
      }}
    >
      {({ allowNavigation }) => {
        const onSave = async (
          { name, description }: ISaveForm,
          actions: any
        ) => {
          const newObj = {};

          Object.keys(state.configuredProperties!).map(key => {
            const value = state.configuredProperties![key];
            /**
             * Syndesis's API doesn't handle arrays well,
             * so we'll be sending it as a JSON encoded array
             * instead.
             */
            if (Array.isArray(value)) {
              newObj[key] = JSON.stringify(value);
            } else {
              newObj[key] = value;
            }
            return newObj;
          });

          try {
            const connection = createConnection(
              connector,
              name,
              description || '',
              newObj
            );
            await saveConnection(connection);

            pushNotification(
              t('connections:connectionCreatedSuccess', { name }),
              'success'
            );
            allowNavigation();
            history.push(resolvers.connections());
          } catch (e) {
            pushNotification(e.message, 'error');
          } finally {
            actions.setSubmitting(false);
          }
        };
        const initialValidator = (values: ISaveForm) =>
          validateRequiredProperties(
            definition,
            (name: string) => t('shared:fieldRequired', { field: name }),
            values
          );
        const validator = async (values: ISaveForm) => {
          const errors = initialValidator(values);
          if (Object.keys(errors).length > 0) {
            return errors;
          }
          const response = await validateName({ name: '' }, values.name);
          if (response.isError) {
            const validationError = { [response.property!]: response.message! };
            throw validationError;
          }
          return errors;
        };
        return (
          <WithLoader
            loading={!hasData}
            loaderChildren={<PageLoader />}
            error={error !== false}
            errorChildren={<ApiError error={error as Error} />}
          >
            {() => (
              <AutoForm<ISaveForm>
                i18nRequiredProperty={t('shared:requiredFieldMessage')}
                definition={definition}
                initialValue={{
                  description: connector.description,
                  name: connector.name,
                }}
                onSave={onSave}
                validate={validator}
                validateInitial={initialValidator}
              >
                {({
                  fields,
                  handleSubmit,
                  isSubmitting,
                  isValid,
                  isValidating,
                  submitForm,
                }) => (
                  <>
                    <PageTitle title={t('connections:create:review:title')} />
                    <ConnectionCreatorBreadcrumb
                      connectionsHref={resolvers.connections()}
                      i18nCancel={t('shared:Cancel')}
                      i18nConnections={t('shared:Connections')}
                      i18nCreateConnection={t('shared:CreateConnection')}
                    />
                    <ConnectionCreatorLayout
                      toggle={
                        <ConnectionCreatorToggleList
                          step={3}
                          i18nSelectConnector={t(
                            'connections:create:connector:title'
                          )}
                          i18nConfigureConnection={t(
                            'connections:create:configure:title'
                          )}
                          i18nNameConnection={t(
                            'connections:create:review:title'
                          )}
                        />
                      }
                      navigation={
                        <ConnectionCreatorBreadSteps
                          step={3}
                          i18nSelectConnector={t(
                            'connections:create:connector:title'
                          )}
                          i18nConfigureConnection={t(
                            'connections:create:configure:title'
                          )}
                          i18nNameConnection={t(
                            'connections:create:review:title'
                          )}
                        />
                      }
                      footer={
                        <ConnectionCreatorFooter
                          backHref={resolvers.create.configureConnector({
                            connector,
                          })}
                          cancelHref={resolvers.connections()}
                          onNext={submitForm}
                          isNextDisabled={!isValid}
                          isNextLoading={isSubmitting || isValidating}
                          isLastStep={true}
                          i18nBack={t('shared:Back')}
                          i18nCancel={t('shared:Cancel')}
                          i18nSave={t('shared:Save')}
                          i18nNext={t('shared:Next')}
                        />
                      }
                      content={
                        <ConnectorConfigurationForm
                          i18nFormTitle={t('connections:create:review:title')}
                          i18nValidate={t('shared:Validate')}
                          isNextDisabled={!isValid}
                          isNextLoading={isSubmitting || isValidating}
                          isValidating={false}
                          handleSubmit={handleSubmit}
                        >
                          {fields}
                        </ConnectorConfigurationForm>
                      }
                    />
                  </>
                )}
              </AutoForm>
            )}
          </WithLoader>
        );
      }}
    </WithLeaveConfirmation>
  );
};
