import { useVirtualization, useVirtualizationHelpers } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import { Breadcrumb, PageSection, VirtualizationDetailsHeader } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { AppContext } from '../../../app';
import resolvers from '../../resolvers';
import { VirtualizationNavBar } from '../shared';
import {
  getOdataUrl,
  getPublishingDetails,
} from '../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization whose details are being shown by this page.
 */
export interface IVirtualizationMetricsPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualizationId - the virtualization whose details are being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationMetricsPageRouteParams#virtualizationId}.
 */

export interface IVirtualizationMetricsPageRouteState {
  virtualization: RestDataService;
}

export const VirtualizationMetricsPage: React.FunctionComponent = () => {

  const { t } = useTranslation(['data', 'shared']);
  const { params } = useRouteData<
    IVirtualizationMetricsPageRouteParams,
    IVirtualizationMetricsPageRouteState
  >();
  const appContext = React.useContext(AppContext);
  const { updateVirtualizationDescription } = useVirtualizationHelpers();
  const { resource: virtualization } = useVirtualization(params.virtualizationId);

  const publishingDetails = getPublishingDetails(
    appContext.config.consoleUrl,
    virtualization
  );

  const doSetDescription = async (newDescription: string) => {
    await updateVirtualizationDescription(
      appContext.user.username || 'developer',
      params.virtualizationId,
      newDescription
    );
    virtualization.tko__description = newDescription;
    return true;
  };

  return (
    <>
      <Breadcrumb>
        <Link
          data-testid={'virtualization-metrics-page-home-link'}
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={
            'virtualization-metrics-page-virtualizations-link'
          }
          to={resolvers.data.root()}
        >
          {t('shared:DataVirtualizations')}
        </Link>
        <span>
          {params.virtualizationId + ' '}
          {t('data:virtualization.metrics')}
        </span>
      </Breadcrumb>
      <VirtualizationDetailsHeader
        i18nDescriptionPlaceholder={t('virtualization.descriptionPlaceholder')}
        i18nDraft={t('shared:Draft')}
        i18nError={t('shared:Error')}
        i18nPublished={t(
          'virtualization.publishedDataVirtualization'
        )}
        i18nPublishInProgress={t(
          'virtualization.publishInProgress'
        )}
        i18nUnpublishInProgress={t(
          'virtualization.unpublishInProgress'
        )}
        i18nPublishLogUrlText={t('shared:viewLogs')}
        odataUrl={getOdataUrl(virtualization)}
        publishedState={publishingDetails.state}
        publishingCurrentStep={publishingDetails.stepNumber}
        publishingLogUrl={publishingDetails.logUrl}
        publishingTotalSteps={publishingDetails.stepTotal}
        publishingStepText={publishingDetails.stepText}
        virtualizationDescription={virtualization.tko__description}
        virtualizationName={virtualization.keng__id}
        isWorking={false}
        onChangeDescription={doSetDescription}
      />
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationNavBar virtualization={virtualization} />
      </PageSection>
      <PageSection>
        <h2>Metrics are not yet implemented</h2>
      </PageSection>
    </>
  );
}
