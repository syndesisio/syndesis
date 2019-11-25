import { useVirtualization } from '@syndesis/api';
import { DvMetricsContainer, PageSection } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
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
   * Hook to obtain the virtualization being edited. Also does polling to get virtualization descriptor updates.
   */
  const { model: virtualization } = useVirtualization(params.virtualizationId);

  return (
    <VirtualizationEditorPage
      routeParams={params}
      routeState={state}
      virtualization={virtualization}
    >
      <PageSection>
        <DvMetricsContainer
          cacheHitProps={{
            a11yInfoCloseButton: 'Close info popover',
            a11yInfoPopover: 'Info popover',
            i18nDatetime: 'Nov 18, 11:40:00 pm',
            i18nInfoMessage: 'Cache hit ratios information message goes here.',
            i18nNoData: 'No data available',
            i18nTitle: 'Cache hit ratios',
            loading: false,
            percentage: '35%',
          }}
          clientSessionProps={{
            connectionCount: 8,
            i18nConnectionMessage: 'Connections are issuing queries',
            i18nNoData: 'No data available',
            i18nTitle: 'Client sessions',
            i18nViewAllAction: 'View all',
            loading: false,
            onViewAll: () => alert('Implement View all'),
          }}
          i18nNoData={'No metrics data available'}
          requestProps={{
            a11yShowFailed: 'Show Failed Requests',
            a11yShowSucceeded: 'Show Succeeded Requests',
            failedCount: 129,
            i18nNoData: 'No data available',
            i18nTitle: 'Total requests',
            loading: false,
            onShowFailed: () => alert('Implement Show Failed'),
            onShowSucceeded: () => alert('Implement Show Succeeded'),
            successCount: 17000,
          }}
          uptimeProps={{
            i18nNoData: 'No data available',
            i18nSinceMessage: 'Since Oct 11, 11:47:14 pm',
            i18nTitle: 'Uptime',
            i18nUptime: '1 day 3 hours 9 minutes',
            loading: false,
          }}
        />
      </PageSection>
    </VirtualizationEditorPage>
  );
};
