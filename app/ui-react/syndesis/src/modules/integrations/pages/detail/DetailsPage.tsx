import { canActivate, canDeactivate, WithIntegration } from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  IMenuActions,
  IntegrationDetailBreadcrumb,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailHistoryListViewItemActions,
  IntegrationDetailInfo,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
import {
  IntegrationDetailSteps,
  WithIntegrationActions,
} from '../../components';
import { IntegrationDetailNavBar } from '../../shared';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IIntegrationDetailsRouteParams {
  integrationId: string;
}

export interface IIntegrationDetailsRouteState {
  integration?: IIntegrationOverviewWithDraft;
}

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IIntegrationDetailsPageProps {
  error: boolean;
  integration: IIntegrationOverviewWithDraft;
  integrationId: string;
  loading: boolean;
}

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class DetailsPage extends React.Component<IIntegrationDetailsPageProps> {
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithRouteData<IIntegrationDetailsRouteParams, null>>
            {({ integrationId }) => (
              <WithIntegration integrationId={integrationId}>
                {({ data, hasData, error }) => (
                  <WithLoader
                    error={error}
                    loading={!hasData}
                    loaderChildren={<Loader />}
                    errorChildren={<ApiError />}
                  >
                    {() => (
                      <WithIntegrationActions integration={data}>
                        {({
                          ciCdAction,
                          editAction,
                          deleteAction,
                          exportAction,
                          startAction,
                          stopAction,
                        }) => {
                          const breadcrumbMenuActions: IMenuActions[] = [];
                          if (canActivate(data)) {
                            breadcrumbMenuActions.push(startAction);
                          }
                          if (canDeactivate(data)) {
                            breadcrumbMenuActions.push(stopAction);
                          }
                          breadcrumbMenuActions.push(deleteAction);
                          breadcrumbMenuActions.push(ciCdAction);
                          return (
                            <>
                              <PageTitle
                                title={t('integrations:detail:pageTitle')}
                              />
                              <IntegrationDetailBreadcrumb
                                editHref={editAction.href}
                                editLabel={editAction.label}
                                exportAction={exportAction.onClick}
                                exportHref={exportAction.href}
                                exportLabel={exportAction.label}
                                homeHref={resolvers.dashboard.root()}
                                i18nHome={t('shared:Home')}
                                i18nIntegrations={t('shared:Integrations')}
                                i18nPageTitle={t(
                                  'integrations:detail:pageTitle'
                                )}
                                integrationId={data.id}
                                integrationsHref={resolvers.integrations.list()}
                                menuActions={breadcrumbMenuActions}
                              />

                              <IntegrationDetailInfo
                                name={data.name}
                                version={data.version}
                              />
                              <IntegrationDetailNavBar integration={data} />
                              <IntegrationDetailSteps integration={data} />
                              <IntegrationDetailDescription
                                description={data.description}
                                i18nNoDescription={t(
                                  'integrations:detail:noDescription'
                                )}
                              />
                              <IntegrationDetailHistoryListView
                                editHref={editAction.href}
                                editLabel={editAction.label}
                                hasHistory={(data.deployments || []).length > 0}
                                isDraft={data.isDraft}
                                i18nTextDraft={t('shared:Draft')}
                                i18nTextHistory={t(
                                  'integrations:detail:History'
                                )}
                                publishAction={
                                  canActivate(data)
                                    ? startAction.onClick
                                    : undefined
                                }
                                publishHref={
                                  canActivate(data)
                                    ? startAction.href
                                    : undefined
                                }
                                publishLabel={
                                  canActivate(data)
                                    ? t('shared:Publish')
                                    : undefined
                                }
                                children={(data.deployments || []).map(
                                  (deployment, idx) => {
                                    return (
                                      <IntegrationDetailHistoryListViewItem
                                        key={idx}
                                        actions={
                                          <IntegrationDetailHistoryListViewItemActions
                                            actions={[]}
                                            integrationId={data.id!}
                                          />
                                        }
                                        currentState={deployment.currentState!}
                                        i18nTextLastPublished={t(
                                          'integrations:detail:lastPublished'
                                        )}
                                        i18nTextVersion={t('shared:Version')}
                                        updatedAt={deployment.updatedAt}
                                        version={deployment.version}
                                      />
                                    );
                                  }
                                )}
                              />
                            </>
                          );
                        }}
                      </WithIntegrationActions>
                    )}
                  </WithLoader>
                )}
              </WithIntegration>
            )}
          </WithRouteData>
        )}
      </Translation>
    );
  }
}
