import {
  WithIntegrationMetrics,
  WithMonitoredIntegration,
} from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import { IntegrationDetailMetrics, Loader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import {
  IntegrationDetailHeader,
  WithIntegrationActions,
} from '../../components';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IMetricsRouteParams {
  integrationId: string;
}

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IMetricsPageProps {
  integrationId: string;
}

export interface IMetricsPageState {
  integration?: IIntegrationOverviewWithDraft;
}

/**
 * This page shows the second tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class MetricsPage extends React.Component<
  IMetricsPageProps,
  IMetricsPageState
> {
  public constructor(props: IMetricsPageProps) {
    super(props);
  }

  public render() {
    return (
      <>
        <Translation ns={['integrations', 'shared']}>
          {t => (
            <AppContext.Consumer>
              {({ getPodLogUrl }) => (
                <WithRouteData<IMetricsRouteParams, null>>
                  {({ integrationId }) => {
                    return (
                      <WithMonitoredIntegration integrationId={integrationId}>
                        {({ data, hasData, error }) => (
                          <WithIntegrationMetrics integrationId={integrationId}>
                            {({ data: metricsData }) => (
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
                                            title={t(
                                              'integrations:detail:pageTitle'
                                            )}
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
                                          <IntegrationDetailMetrics
                                            i18nUptime={t(
                                              'integrations:metrics:uptime'
                                            )}
                                            i18nTotalMessages={t(
                                              'integrations:metrics:totalMessages'
                                            )}
                                            i18nTotalErrors={t(
                                              'integrations:metrics:totalErrors'
                                            )}
                                            i18nSince={t(
                                              'integrations:metrics:since'
                                            )}
                                            i18nLastProcessed={t(
                                              'integrations:metrics:lastProcessed'
                                            )}
                                            errors={metricsData.errors}
                                            lastProcessed={
                                              metricsData.lastProcessed
                                            }
                                            messages={metricsData.messages}
                                            start={metricsData.start}
                                          />
                                        </>
                                      );
                                    }}
                                  </WithIntegrationActions>
                                )}
                              </WithLoader>
                            )}
                          </WithIntegrationMetrics>
                        )}
                      </WithMonitoredIntegration>
                    );
                  }}
                </WithRouteData>
              )}
            </AppContext.Consumer>
          )}
        </Translation>
      </>
    );
  }
}
