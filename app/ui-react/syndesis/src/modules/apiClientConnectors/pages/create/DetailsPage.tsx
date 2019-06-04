import { useApiConnectorCreator } from '@syndesis/api';
import * as H from '@syndesis/history';
import { APISummary } from '@syndesis/models';
import {
  ApiConnectorCreatorLayout,
  ButtonLink,
  Loader,
  PageSection,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorWizardSteps,
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
        const handleSubmit = async (values: IConnectorValues, actions: any) => {
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
            <ApiConnectorCreatorBreadcrumb cancelHref={resolvers.list()} />
            <ApiConnectorCreatorLayout
              header={<ApiConnectorCreatorWizardSteps step={4} />}
              content={
                <PageSection>
                  <ApiConnectorInfoForm
                    name={state.specification.name}
                    description={state.specification.description}
                    isEditing={true}
                    handleSubmit={handleSubmit}
                  >
                    {({ submitForm, isSubmitting, isUploadingImage }) => (
                      <>
                        <ButtonLink
                          data-testid={
                            'api-connector-details-form-cancel-button'
                          }
                          className="api-connector-details-form__editButton"
                          href={resolvers.create.security(state)}
                        >
                          {t('shared:Back')}
                        </ButtonLink>
                        <ButtonLink
                          data-testid={'api-connector-details-form-save-button'}
                          as={'primary'}
                          className={'api-connector-details-form__editButton'}
                          disabled={isSubmitting || isUploadingImage}
                          onClick={submitForm}
                        >
                          {(isSubmitting || isUploadingImage) && (
                            <Loader size={'sm'} inline={true} />
                          )}
                          {t('shared:Save')}
                        </ButtonLink>
                      </>
                    )}
                  </ApiConnectorInfoForm>
                </PageSection>
              }
            />
          </>
        );
      }}
    </WithLeaveConfirmation>
  );
};
