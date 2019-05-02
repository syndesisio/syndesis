import { canActivate, WithMonitoredIntegration } from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailHistoryListViewItemActions,
  Loader,
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

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IIntegrationDetailsRouteParams {
  integrationId: string;
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
          <AppContext.Consumer>
            {({ getPodLogUrl }) => (
              <WithRouteData<IIntegrationDetailsRouteParams, null>>
                {({ integrationId }) => (
                  <WithMonitoredIntegration integrationId={integrationId}>
                    {({ data, hasData, error }) => (
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<Loader />}
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
                                  <IntegrationDetailDescription
                                    description={data.integration.description}
                                    i18nNoDescription={t(
                                      'integrations:detail:noDescription'
                                    )}
                                  />
                                  <IntegrationDetailHistoryListView
                                    editHref={editAction.href}
                                    editLabel={editAction.label}
                                    hasHistory={
                                      (data.integration.deployments || [])
                                        .length > 0
                                    }
                                    isDraft={
                                      (data.integration as IIntegrationOverviewWithDraft)
                                        .isDraft
                                    }
                                    i18nTextDraft={t('shared:Draft')}
                                    i18nTextHistory={t(
                                      'integrations:detail:History'
                                    )}
                                    publishAction={
                                      canActivate(data.integration)
                                        ? startAction.onClick
                                        : undefined
                                    }
                                    publishHref={
                                      canActivate(data.integration)
                                        ? startAction.href
                                        : undefined
                                    }
                                    publishLabel={
                                      canActivate(data.integration)
                                        ? t('shared:Publish')
                                        : undefined
                                    }
                                    children={(
                                      data.integration.deployments || []
                                    ).map((deployment, idx) => {
                                      return (
                                        <IntegrationDetailHistoryListViewItem
                                          key={idx}
                                          actions={
                                            <IntegrationDetailHistoryListViewItemActions
                                              actions={[]}
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
                                          i18nTextVersion={t('shared:Version')}
                                          updatedAt={deployment.updatedAt}
                                          version={deployment.version}
                                        />
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
    );
  }
}
