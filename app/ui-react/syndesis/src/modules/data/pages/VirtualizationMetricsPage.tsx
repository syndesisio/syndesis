import { useVirtualization, useVirtualizationMetrics } from '@syndesis/api';
import {
  DvMetricsContainer,
  DvMetricsContainerSkeleton,
  PageSection,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import { WithLoader } from '@syndesis/utils';
import {
  toShortDateAndTimeString,
  toUptimeDurationString,
} from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

/**
 * A page that displays virtualization publish state and history.
 */
export const VirtualizationMetricsPage: React.FunctionComponent = () => {
  /**
   * Hook to obtain route params and history.
   */
  const { params, state } = useRouteData<
    IVirtualizationEditorPageRouteParams,
    IVirtualizationEditorPageRouteState
  >();

  /**
   * Hook to handle localization.
   */
  const { t } = useTranslation(['data', 'shared']);

  /**
   * Hook to obtain the virtualization being edited. Also does polling to get virtualization descriptor updates.
   */
  const { model: virtualization } = useVirtualization(params.virtualizationId);

  /**
   * Hook to obtain virtualization editions.
   */
  const { resource: metrics, hasData, error } = useVirtualizationMetrics(
    params.virtualizationId
  );

  /**
   * Get the uptime string for display
   * @param utcTimestamp the utc timestamp string
   * @returns the Uptime display
   */
  const getUptimeDisplay = (utcTimestamp: string): string => {
    const numericTimestamp = Date.parse(utcTimestamp);
    const duration = Date.now() - numericTimestamp;
    return toUptimeDurationString(duration);
  };

  /**
   * Get the date and time display
   * @param utcTimestamp the utc timestamp string
   * @returns the Date and time display
   */
  const getDateAndTimeDisplay = (utcTimestamp: string): string => {
    const numericTimestamp = Date.parse(utcTimestamp);
    return toShortDateAndTimeString(numericTimestamp);
  };

  return (
    <VirtualizationEditorPage
      routeParams={params}
      routeState={state}
      virtualization={virtualization}
    >
      <PageSection>
        <WithLoader
          error={error !== false}
          loading={!hasData}
          loaderChildren={<DvMetricsContainerSkeleton />}
          errorChildren={
            <DvMetricsContainer
              i18nNoDataTitle={t('metricsNoMetricsTitle')}
              i18nNoDataDescription={t('metricsNoMetricsDescription')}
            />
          }
        >
          {() => {
            return virtualization.publishedState === 'RUNNING' ? (
              <DvMetricsContainer
                resultSetCacheProps={{
                  a11yInfoCloseButton: t('metricsCacheHitRatioA11yInfoClose'),
                  a11yInfoPopover: t('metricsCacheHitRatioA11yInfo'),
                  cacheHitRatioPercentage: t('metricsCacheHitRatioPct', {
                    pctValue: metrics.resultSetCacheHitRatio * 100,
                  }),
                  i18nCacheHitRatioText: t('metricsCacheHitRatioText'),
                  i18nInfoMessage: t('metricsCacheHitRatioDescription'),
                  i18nNoData: t('metricsNoDataAvailable'),
                  i18nTitle: t('metricsResultSetCacheTitle'),
                  loading: !hasData,
                }}
                clientSessionProps={{
                  i18nNoData: t('metricsNoDataAvailable'),
                  i18nSessionText: t('metricsClientSessionsText'),
                  i18nTitle: t('metricsClientSessionsTitle'),
                  loading: !hasData,
                  sessionCount: metrics.sessions,
                }}
                i18nNoDataTitle={t('metricsNoMetricsTitle')}
                i18nNoDataDescription={t('metricsNoMetricsDescription')}
                requestProps={{
                  i18nNoData: t('metricsNoDataAvailable'),
                  i18nRequestText: t('metricsRequestCountText'),
                  i18nTitle: t('metricsRequestCountTitle'),
                  loading: !hasData,
                  requestCount: metrics.requestCount,
                }}
                uptimeProps={{
                  i18nNoData: t('metricsNoDataAvailable'),
                  i18nSinceMessage: t('metricsUptimeSince', {
                    sinceTime: getDateAndTimeDisplay(metrics.startedAt),
                  }),
                  i18nTitle: t('metricsUptimeTitle'),
                  i18nUptime: getUptimeDisplay(metrics.startedAt),
                  loading: !hasData,
                }}
              />
            ) : (
              <DvMetricsContainer
                i18nNoDataTitle={t('metricsNoMetricsTitle')}
                i18nNoDataDescription={t('metricsNoMetricsDescription')}
              />
            );
          }}
        </WithLoader>
      </PageSection>
    </VirtualizationEditorPage>
  );
};
