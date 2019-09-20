import { useVirtualizationHelpers } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import {
  Breadcrumb,
  PageSection,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { AppContext, UIContext } from '../../../app';
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
  const { params, state } = useRouteData<
    IVirtualizationMetricsPageRouteParams,
    IVirtualizationMetricsPageRouteState
  >();
  const [description, setDescription] = React.useState(
    state.virtualization.tko__description
  );
  const appContext = React.useContext(AppContext);
  const { pushNotification } = React.useContext(UIContext);
  const { updateVirtualizationDescription } = useVirtualizationHelpers();

  const publishingDetails = getPublishingDetails(
    appContext.config.consoleUrl,
    state.virtualization
  );

  const getUsedByMessage = (integrationNames: string[]): string => {
    if (integrationNames.length === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: integrationNames.length });
  };
  
  const doSetDescription = async (newDescription: string) => {
    const previous = description;
    setDescription(newDescription); // this sets InlineTextEdit component to new value
    try {
      await updateVirtualizationDescription(
        appContext.user.username || 'developer',
        params.virtualizationId,
        newDescription
      );
      state.virtualization.tko__description = newDescription;
      return true;
    } catch {
      pushNotification(
        t('virtualization.errorUpdatingDescription', {
          name: state.virtualization.keng__id,
        }),
        'error'
      );
      setDescription(previous); // save failed so set InlineTextEdit back to old value
      return false;
    }
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
          data-testid={'virtualization-metrics-page-virtualizations-link'}
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
        i18nInUseText={getUsedByMessage(state.virtualization.usedBy)}
        i18nPublished={t('virtualization.publishedDataVirtualization')}
        i18nPublishInProgress={t('virtualization.publishInProgress')}
        i18nUnpublishInProgress={t('virtualization.unpublishInProgress')}
        i18nPublishLogUrlText={t('shared:viewLogs')}
        odataUrl={getOdataUrl(state.virtualization)}
        publishedState={publishingDetails.state}
        publishingCurrentStep={publishingDetails.stepNumber}
        publishingLogUrl={publishingDetails.logUrl}
        publishingTotalSteps={publishingDetails.stepTotal}
        publishingStepText={publishingDetails.stepText}
        virtualizationDescription={description}
        virtualizationName={state.virtualization.keng__id}
        isWorking={false}
        onChangeDescription={doSetDescription}
      />
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationNavBar virtualization={state.virtualization} />
      </PageSection>
      <PageSection>
        <h2>Metrics are not yet implemented</h2>
      </PageSection>
    </>
  );
};
