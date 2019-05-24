import {
  WithIntegrationHelpers,
  WithMonitoredIntegration,
} from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  InlineTextEdit,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailHistoryListViewItemActions,
  PageLoader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import {
  IntegrationDetailHeader,
  IntegrationDetailSteps,
  WithIntegrationActions,
} from '../../components';
import { WithDeploymentActions } from '../../components/WithDeploymentActions';
import { IDetailsRouteParams, IDetailsRouteState } from './interfaces';

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class DetailsPage extends React.Component {
  public render() {
    return (
      <>
        <Translation ns={['integrations', 'shared']}>
          {t => (
            <AppContext.Consumer>
              {({ getPodLogUrl }) => (
                <WithRouteData<IDetailsRouteParams, IDetailsRouteState>>
                  {({ integrationId }, { integration }) => (
                    <WithMonitoredIntegration
                      integrationId={integrationId}
                      initialValue={integration}
                    >
                      {({ data, hasData, error }) => (
                        <WithLoader
                          error={error}
                          loading={!hasData}
                          loaderChildren={<PageLoader />}
                          errorChildren={<ApiError />}
                        >
                          {() => (
                            <WithIntegrationActions
                              integration={data.integration}
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
                                    <IntegrationDetailSteps
                                      integration={data.integration}
                                    />
                                    <WithIntegrationHelpers>
                                      {({ setAttributes }) => {
                                        const handleChange = async (
                                          description: string
                                        ) => {
                                          try {
                                            await setAttributes(
                                              data.integration.id!,
                                              {
                                                description,
                                              }
                                            );
                                            return true;
                                          } catch (err) {
                                            return false;
                                          }
                                        };
                                        return (
                                          <IntegrationDetailDescription
                                            description={
                                              <InlineTextEdit
                                                value={
                                                  data.integration
                                                    .description ||
                                                  t(
                                                    'integrations:detail:noDescription'
                                                  )
                                                }
                                                allowEditing={true}
                                                isTextArea={true}
                                                onChange={handleChange}
                                              />
                                            }
                                          />
                                        );
                                      }}
                                    </WithIntegrationHelpers>
                                    <IntegrationDetailHistoryListView
                                      editHref={editAction.href}
                                      editLabel={editAction.label}
                                      hasHistory={
                                        (data.integration.deployments || [])
                                          .length > 0
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
                                      children={(
                                        data.integration.deployments || []
                                      )
                                        .sort((a, b) => {
                                          const aVersion =
                                            (a || {}).version || 0;
                                          const bVersion =
                                            (b || {}).version || 0;
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
                                              integrationId={
                                                data.integration.id!
                                              }
                                              integrationName={
                                                data.integration.name
                                              }
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
                                                  actions.push(
                                                    replaceDraftAction
                                                  );
                                                }
                                                if (
                                                  data.integration.version ===
                                                    deployment.version &&
                                                  data.integration
                                                    .currentState ===
                                                    'Published'
                                                ) {
                                                  actions.push(
                                                    stopDeploymentAction
                                                  );
                                                } else {
                                                  actions.push(
                                                    startDeploymentAction
                                                  );
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
            </AppContext.Consumer>
          )}
        </Translation>
      </>
    );
  }
}
