import { useConnectionHelpers, useConnector } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import { IConnector } from '@syndesis/models';
import {
  ConnectionCreatorLayout,
  ConnectorConfigurationForm,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { useRouteData, validateRequiredProperties, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import { ConnectionCreatorBreadSteps } from '../../components';
import { ConnectionCreatorBreadcrumb } from '../../components/ConnectionCreatorBreadcrumb';
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
  const { createConnection, saveConnection } = useConnectionHelpers();

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
          try {
            const connection = createConnection(
              connector,
              name,
              description || '',
              state.configuredProperties
            );
            await saveConnection(connection);
            pushNotification(
              `<strong>Connection created</strong> Connection <strong>${name}</strong> successfully created`,
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
        const validator = (values: ISaveForm) =>
          validateRequiredProperties(
            definition,
            (name: string) => `${name} is required`,
            values
          );

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
                validateInitial={validator}
              >
                {({
                  fields,
                  handleSubmit,
                  isSubmitting,
                  isValid,
                  submitForm,
                }) => (
                  <>
                    <PageTitle title={t('connections:create:review:title')} />
                    <ConnectionCreatorBreadcrumb
                      cancelHref={resolvers.connections()}
                    />
                    <ConnectionCreatorLayout
                      header={<ConnectionCreatorBreadSteps step={3} />}
                      content={
                        <PageSection>
                          <ConnectorConfigurationForm
                            i18nFormTitle={t('connections:create:review:title')}
                            handleSubmit={handleSubmit}
                            backHref={resolvers.create.configureConnector({
                              connector,
                            })}
                            onNext={submitForm}
                            isNextDisabled={!isValid}
                            isNextLoading={isSubmitting}
                            isValidating={false}
                            isLastStep={true}
                            i18nSave={t('shared:Save')}
                            i18nNext={t('shared:Next')}
                          >
                            {fields}
                          </ConnectorConfigurationForm>
                        </PageSection>
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
