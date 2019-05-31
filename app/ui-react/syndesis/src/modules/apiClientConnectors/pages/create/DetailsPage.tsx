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
import { Translation } from 'react-i18next';
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
  specification: APISummary;
}

export const DetailsPage: React.FunctionComponent = () => {
  const { state } = useRouteData<null, IDetailsPageRouteState>();

  const handleSubmit = async (values: IConnectorValues, actions: any) => {
    actions.setSubmitting(true);
    try {
      // tslint:disable-next-line
      console.log(values);
      actions.setSubmitting(false);
      return true;
    } catch (e) {
      actions.setSubmitting(false);
      return false;
    }
  };

  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {t => (
        <WithLeaveConfirmation
          i18nTitle={t('unsavedChangesTitle')}
          i18nConfirmationMessage={t('unsavedChangesMessage')}
          shouldDisplayDialog={(location: H.LocationDescriptor) => {
            const url =
              typeof location === 'string' ? location : location.pathname!;
            return !url.startsWith(routes.create.root);
          }}
        >
          {() => (
            <>
              <PageTitle
                title={t('apiClientConnectors:create:details:title')}
              />
              <ApiConnectorCreatorBreadcrumb cancelHref={resolvers.list()} />
              <ApiConnectorCreatorLayout
                header={<ApiConnectorCreatorWizardSteps step={4} />}
                content={
                  <PageSection>
                    <ApiConnectorInfoForm
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
                            {t('shared:Cancel')}
                          </ButtonLink>
                          <ButtonLink
                            data-testid={
                              'api-connector-details-form-save-button'
                            }
                            as="primary"
                            className="api-connector-details-form__editButton"
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
          )}
        </WithLeaveConfirmation>
      )}
    </Translation>
  );
};
