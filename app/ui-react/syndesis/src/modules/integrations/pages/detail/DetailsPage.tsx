import {
  getApiProviderFlows,
  isIntegrationApiProvider,
  useIntegrationHelpers,
  WithMonitoredIntegration,
} from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  InlineTextEdit,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailHistoryListViewItemActions,
  IntegrationExposedURL,
  PageLoader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import {AppContext, UIContext} from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import {
  IntegrationDetailApiProviderSteps,
  IntegrationDetailHeader,
  IntegrationDetailSteps,
  WithIntegrationActions,
} from '../../components';
import { IntegrationExposeVia } from '../../components/IntegrationExposeVia';
import { WithDeploymentActions } from '../../components/WithDeploymentActions';
import resolvers from '../../resolvers';
import { IDetailsRouteParams, IDetailsRouteState } from './interfaces';

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */

export const DetailsPage: React.FunctionComponent = () => {
  const { getPodLogUrl } = React.useContext(AppContext);
  const { setAttributes, deployIntegration } = useIntegrationHelpers();
  const handleDescriptionChange = async (id: string, description: string) => {
    try {
      await setAttributes(id, {
        description,
      });
      return true;
    } catch (err) {
      return false;
    }
  };
  return (
    <>
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithRouteData<IDetailsRouteParams, IDetailsRouteState>>
            {({ integrationId }, { integration }) => (
              <WithMonitoredIntegration
                integrationId={integrationId}
                initialValue={integration}
              >
                {({ data, hasData, error, errorMessage }) => (
                  <WithLoader
                    error={error}
                    loading={!hasData}
                    loaderChildren={<PageLoader />}
                    errorChildren={<ApiError error={errorMessage!} />}
                  >
                    {() => (
                      <WithIntegrationActions
                        integration={data.integration}
                        postDeleteHref={resolvers.list()}
                      >
                        {({
                          ciCdAction,
                          editAction,
                          deleteAction,
                          exportAction,
                          startAction,
                          stopAction,
                        }) => {
                          return (
                            <>
                              <PageTitle
                                title={t('integrations:detail:pageTitle')}
                              />
                              <IntegrationDetailHeader
                                data={data}
                                startAction={startAction}
                                stopAction={stopAction}
                                deleteAction={deleteAction}
                                ciCdAction={ciCdAction}
                                editAction={editAction}
                                exportAction={exportAction}
                                getPodLogUrl={getPodLogUrl}
                              />
                              {isIntegrationApiProvider(data.integration) ? (
                                <IntegrationDetailApiProviderSteps
                                  flowCount={getApiProviderFlows(data.integration).length}
                                />
                              ) : (
                                <IntegrationDetailSteps
                                  integration={data.integration}
                                />
                              )}
                              <IntegrationExposedURL
                                url={data.integration.url}
                              />
                              <UIContext.Consumer>
                                {({ pushNotification }) => {
                                  const handleChange = async (exposure: string) => {
                                    try {
                                      await setAttributes(data.integration.id!, {
                                        exposure,
                                      });
                                    } catch (err) {
                                      return false;
                                    }

                                    if (data.integration.currentState !== 'Unpublished') {
                                      try {
                                        pushNotification(
                                          t('integrations:PublishingIntegrationMessage'),
                                          'info'
                                        );
                                        await deployIntegration(
                                          data.integration.id!,
                                          data.integration.version!,
                                          false
                                        );
                                      } catch (err) {
                                        pushNotification(
                                          t(
                                            'integrations:PublishingIntegrationFailedMessage',
                                            {
                                              error:
                                                err.errorMessage ||
                                                err.message ||
                                                err,
                                            }
                                          ),
                                          'warning'
                                        );
                                        return false;
                                      }
                                    }

                                    return true;
                                  };

                                  return (
                                    <IntegrationExposeVia
                                      integration={data.integration} onChange={handleChange}
                                    />
                                  );
                                }}
                              </UIContext.Consumer>
                              <IntegrationDetailDescription
                                description={
                                  <InlineTextEdit
                                    value={
                                      data.integration.description ||
                                      t('integrations:detail:noDescription')
                                    }
                                    allowEditing={true}
                                    isTextArea={true}
                                    onChange={value =>
                                      handleDescriptionChange(
                                        data.integration.id!,
                                        value
                                      )
                                    }
                                  />
                                }
                              />
                              <IntegrationDetailHistoryListView
                                editHref={editAction.href}
                                editLabel={editAction.label}
                                hasHistory={
                                  (data.integration.deployments || []).length >
                                  0
                                }
                                isDraft={
                                  (data.integration as IIntegrationOverviewWithDraft)
                                    .isDraft || false
                                }
                                i18nTextDraft={t('shared:Draft')}
                                i18nTextHistory={t(
                                  'integrations:detail:History'
                                )}
                                publishAction={startAction.onClick}
                                publishLabel={t('shared:Publish')}
                                children={(data.integration.deployments || [])
                                  .sort((a, b) => {
                                    const aVersion = (a || {}).version || 0;
                                    const bVersion = (b || {}).version || 0;
                                    return bVersion - aVersion;
                                  })
                                  .map((deployment, idx) => {
                                    const updatedAt = deployment.updatedAt
                                      ? new Date(
                                          deployment.updatedAt!
                                        ).toLocaleString()
                                      : '';
                                    return (
                                      <WithDeploymentActions
                                        key={deployment.id}
                                        integrationId={data.integration.id!}
                                        integrationName={data.integration.name}
                                        deployment={deployment}
                                      >
                                        {({
                                          startDeploymentAction,
                                          stopDeploymentAction,
                                          replaceDraftAction,
                                        }) => {
                                          const actions = [];
                                          if (
                                            data.integration.version !==
                                            deployment.version
                                          ) {
                                            actions.push(replaceDraftAction);
                                          }
                                          if (
                                            data.integration.version ===
                                              deployment.version &&
                                            data.integration.currentState ===
                                              'Published'
                                          ) {
                                            actions.push(stopDeploymentAction);
                                          } else {
                                            actions.push(startDeploymentAction);
                                          }
                                          return (
                                            <IntegrationDetailHistoryListViewItem
                                              key={deployment.id}
                                              actions={
                                                <IntegrationDetailHistoryListViewItemActions
                                                  actions={actions}
                                                  integrationId={
                                                    data.integration.id!
                                                  }
                                                />
                                              }
                                              currentState={
                                                deployment.currentState!
                                              }
                                              i18nTextLastPublished={t(
                                                'integrations:detail:lastPublished'
                                              )}
                                              i18nTextVersion={t(
                                                'shared:Version'
                                              )}
                                              updatedAt={updatedAt}
                                              version={deployment.version}
                                            />
                                          );
                                        }}
                                      </WithDeploymentActions>
                                    );
                                  })}
                              />
                            </>
                          );
                        }}
                      </WithIntegrationActions>
                    )}
                  </WithLoader>
                )}
              </WithMonitoredIntegration>
            )}
          </WithRouteData>
        )}
      </Translation>
    </>
  );
};
