import { useApiConnectorCreator } from '@syndesis/api';
import * as H from '@syndesis/history';
import { APISummary } from '@syndesis/models';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorLayout,
  ApiConnectorCreatorToggleList,
  ApiConnectorDetailsForm
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import {
  ApiConnectorInfoForm,
  IConnectorValues,
} from '../../components';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface IDetailsPageRouteState {
  authenticationType?: string;
  authorizationEndpoint?: string;
  specification: APISummary;
  tokenEndpoint?: string;
}

export const DetailsPage: React.FunctionComponent = () => {
  const { t } = useTranslation(['apiClientConnectors', 'shared']);
  const { pushNotification } = React.useContext(UIContext);
  const { state, history } = useRouteData<null, IDetailsPageRouteState>();
  const createApiConnector = useApiConnectorCreator();

  return (
    <WithLeaveConfirmation
      i18nTitle={t('apiClientConnectors:create:unsavedChangesTitle')}
      i18nConfirmationMessage={t(
        'apiClientConnectors:create:unsavedChangesMessage'
      )}
      shouldDisplayDialog={(location: H.LocationDescriptor) => {
        const url =
          typeof location === 'string' ? location : location.pathname!;
        return !url.startsWith(routes.create.root);
      }}
    >
      {({ allowNavigation }) => {
        const onSubmit = async (values: IConnectorValues, actions: any) => {
          actions.setSubmitting(true);
          try {
            // tslint:disable-next-line
            await createApiConnector({
              ...values,
              authenticationType: state.authenticationType,
              authorizationEndpoint: state.authorizationEndpoint,
              specification: state.specification.configuredProperties!
                .specification,
              tokenEndpoint: state.tokenEndpoint,
            });
            actions.setSubmitting(false);
            allowNavigation();
            history.push(resolvers.list());
            pushNotification(
              t('apiClientConnectors:create:details:successNotification'),
              'success'
            );
            return true;
          } catch (e) {
            actions.setSubmitting(false);
            pushNotification(e.message, 'error');
            return false;
          }
        };

        return (
          <>
            <PageTitle title={t('apiClientConnectors:create:details:title')} />
            <ApiConnectorCreatorBreadcrumb
              cancelHref={resolvers.list()}
              connectorsHref={resolvers.list()}
              i18nCancel={t('shared:Cancel')}
              i18nConnectors={t('apiClientConnectors:apiConnectorsPageTitle')}
              i18nCreateConnection={t('apiClientConnectors:CreateApiConnector')}
            />
            <ApiConnectorInfoForm
              name={state.specification.name}
              description={state.specification.description}
              handleSubmit={onSubmit}
            >
              {({
                  connectorName,
                  fields,
                  handleSubmit,
                  icon,
                  isSubmitting,
                  isUploadingImage,
                  onUploadImage,
                  submitForm
              }) => (
                <ApiConnectorCreatorLayout
                  content={
                    <div style={{ maxWidth: '600px' }}>
                      <ApiConnectorDetailsForm
                        apiConnectorIcon={icon}
                        apiConnectorName={connectorName}
                        i18nIconLabel={t('ConnectorIcon')}
                        handleSubmit={handleSubmit}
                        onUploadImage={onUploadImage}
                        isEditing={true}
                        fields={fields}
                      />
                    </div>
                  }
                  footer={
                    <ApiConnectorCreatorFooter
                      backHref={resolvers.create.security(state)}
                      onNext={submitForm}
                      i18nBack={t('shared:Back')}
                      i18nNext={t('shared:Save')}
                      isNextLoading={isSubmitting || isUploadingImage}
                      isNextDisabled={isSubmitting || isUploadingImage}
                    />
                  }
                  navigation={
                    <ApiConnectorCreatorBreadSteps
                      step={4}
                      i18nDetails={t('apiClientConnectors:create:details:title')}
                      i18nReview={t('apiClientConnectors:create:review:title')}
                      i18nSecurity={t('apiClientConnectors:create:security:title')}
                      i18nSelectMethod={t('apiClientConnectors:create:selectMethod:title')}
                    />
                  }
                  toggle={
                    <ApiConnectorCreatorToggleList
                      step={1}
                      i18nDetails={t('apiClientConnectors:create:details:title')}
                      i18nReview={t('apiClientConnectors:create:review:title')}
                      i18nSecurity={t('apiClientConnectors:create:security:title')}
                      i18nSelectMethod={t('apiClientConnectors:create:selectMethod:title')}
                    />
                  }
                />
              )}
            </ApiConnectorInfoForm>
          </>
        );
      }}
    </WithLeaveConfirmation>
  );
};
