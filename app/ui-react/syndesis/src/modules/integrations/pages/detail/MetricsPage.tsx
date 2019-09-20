import {
  WithIntegrationMetrics,
  WithMonitoredIntegration,
} from '@syndesis/api';
import { IntegrationDetailMetrics, PageLoader } from '@syndesis/ui';
import {
  toUptimeDurationString,
  WithLoader,
  WithRouteData,
} from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import {
  IntegrationDetailHeader,
  WithIntegrationActions,
} from '../../components';
import resolvers from '../../resolvers';
import { IDetailsRouteParams, IDetailsRouteState } from './interfaces';

/**
 * This page shows the third tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class MetricsPage extends React.Component {
  public render() {
    return (
      <>
        <Translation ns={['integrations', 'shared']}>
          {t => (
            <AppContext.Consumer>
              {({ getPodLogUrl }) => (
                <WithRouteData<IDetailsRouteParams, IDetailsRouteState>>
                  {({ integrationId }, { integration }) => {
                    return (
                      <WithMonitoredIntegration
                        integrationId={integrationId}
                        initialValue={integration}
                      >
                        {({ data, hasData, error, errorMessage }) => (
                          <WithIntegrationMetrics integrationId={integrationId}>
                            {({ data: metricsData }) => (
                              <WithLoader
                                error={error}
                                loading={!hasData}
                                loaderChildren={<PageLoader />}
                                errorChildren={
                                  <ApiError error={errorMessage!} />
                                }
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
                                            i18nUptime={t('metrics.uptime')}
                                            i18nTotalMessages={t(
                                              'metrics.totalMessages'
                                            )}
                                            i18nTotalErrors={t(
                                              'metrics.totalErrors'
                                            )}
                                            i18nSince={t('metrics.since')}
                                            i18nLastProcessed={t(
                                              'metrics.lastProcessed'
                                            )}
                                            i18nNoDataAvailable={t(
                                              'metrics.NoDataAvailable'
                                            )}
                                            errors={metricsData.errors}
                                            lastProcessed={
                                              metricsData.lastProcessed !==
                                              undefined
                                                ? new Date(
                                                    parseInt(
                                                      metricsData.lastProcessed,
                                                      10
                                                    )
                                                  ).toLocaleString()
                                                : t('metrics.NoDataAvailable')
                                            }
                                            messages={metricsData.messages}
                                            start={
                                              typeof metricsData.start ===
                                              'undefined'
                                                ? undefined
                                                : parseInt(
                                                    metricsData.start!,
                                                    10
                                                  )
                                            }
                                            uptimeDuration={
                                              metricsData.uptimeDuration! > 0
                                                ? toUptimeDurationString(
                                                    metricsData.uptimeDuration!,
                                                    t('metrics.NoDataAvailable')
                                                  )
                                                : undefined
                                            }
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
