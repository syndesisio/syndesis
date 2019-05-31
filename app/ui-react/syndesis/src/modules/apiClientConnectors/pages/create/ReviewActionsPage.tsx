import { useApiProviderSummary } from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  ApiConnectorCreatorLayout,
  ButtonLink,
  OpenApiReviewActions,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorWizardSteps,
} from '../../components';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface IReviewActionsRouteState {
  specification: string;
}

export const ReviewActionsPage: React.FunctionComponent = () => {
  const uiContext = React.useContext(UIContext);
  const { state, history } = useRouteData<null, IReviewActionsRouteState>();
  const { apiSummary, loading, error } = useApiProviderSummary(
    state.specification
  );

  React.useEffect(() => {
    if (error) {
      uiContext.pushNotification((error as Error).message, 'error');
      history.push(resolvers.create.upload());
    }
  }, [error, uiContext, history]);

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
              <PageTitle title={t('apiClientConnectors:create:review:title')} />
              <ApiConnectorCreatorBreadcrumb cancelHref={resolvers.list()} />
              <ApiConnectorCreatorLayout
                header={<ApiConnectorCreatorWizardSteps step={2} />}
                content={
                  <PageSection>
                    <WithLoader
                      loading={loading}
                      loaderChildren={<PageLoader />}
                      error={error !== false}
                      errorChildren={<></>}
                    >
                      {() => (
                        <>
                          <OpenApiReviewActions
                            i18nApiDefinitionHeading={'API DEFINITION'}
                            i18nDescriptionLabel={'Description'}
                            i18nImportedHeading={'IMPORTED'}
                            i18nNameLabel={'Name'}
                            apiProviderDescription={apiSummary!.description}
                            apiProviderName={apiSummary!.name}
                            i18nOperationsHtmlMessage={`<strong>${
                              apiSummary!.actionsSummary!.totalActions
                            }</strong> operations`}
                            i18nWarningsHeading={'WARNINGS'}
                            warningMessages={
                              apiSummary!.warnings
                                ? apiSummary!.warnings.map(
                                    warning => (warning as any).message
                                  )
                                : undefined
                            }
                          />
                          <div>
                            <ButtonLink href={resolvers.create.upload()}>
                              Back
                            </ButtonLink>
                            &nbsp;&nbsp;&nbsp;
                            <ButtonLink
                              href={resolvers.create.specification({
                                specification: apiSummary!.configuredProperties!
                                  .specification,
                              })}
                            >
                              Review/Edit
                            </ButtonLink>
                            &nbsp;
                            <ButtonLink
                              as={'primary'}
                              href={resolvers.create.security({
                                specification: apiSummary!,
                              })}
                            >
                              Next
                            </ButtonLink>
                          </div>
                        </>
                      )}
                    </WithLoader>
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
