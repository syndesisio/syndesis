import { useVirtualizationConnectionStatuses } from '@syndesis/api';
import { Virtualization } from '@syndesis/models';
import { ViewsImportLayout, ViewWizardHeader } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
import { DvConnectionsWithToolbar } from '../../shared';

/**
 * @param virtualizationId - the ID of the virtualization for the wizard
 */
export interface ISelectConnectionRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 */
export interface ISelectConnectionRouteState {
  virtualization: Virtualization;
}

export interface ISelectConnectionPageProps {
  selectedConnection: string;
  handleConnectionSelectionChanged: (name: string, selected: boolean) => void;
}

export const SelectConnectionPage: React.FunctionComponent<
  ISelectConnectionPageProps
> = props => {
  const { state } = useRouteData<
    ISelectConnectionRouteParams,
    ISelectConnectionRouteState
  >();

  const { t } = useTranslation(['data', 'shared']);

  const connectionId = props.selectedConnection;
  const virtualization = state.virtualization;
  const {
    resource: connectionStatuses,
    hasData: hasConnectionStatuses,
    error: connectionStatusesError,
  } = useVirtualizationConnectionStatuses();

  return (
    <>
      <PageTitle title={t('importViewsPageTitle')} />
      <ViewsImportLayout
        header={
          <ViewWizardHeader
            step={1}
            cancelHref={resolvers.data.virtualizations.views.root({
              virtualization,
            })}
            nextHref={resolvers.data.virtualizations.views.importSource.selectViews({
              connectionId,
              virtualization,
            })}
            isNextDisabled={props.selectedConnection.length < 1}
            isNextLoading={false}
            isLastStep={false}
            i18nStep1Text={t('data:importDataSourceWizardStep1')}
            i18nStep2Text={t('data:importDataSourceWizardStep2')}
            i18nBack={t('shared:Back')}
            i18nDone={t('shared:Done')}
            i18nNext={t('shared:Next')}
            i18nCancel={t('shared:Cancel')}
          />
        }
        content={
          <DvConnectionsWithToolbar
            error={connectionStatusesError !== false}
            errorMessage={
              connectionStatusesError === false
                ? undefined
                : (connectionStatusesError as Error).message
            }
            loading={!hasConnectionStatuses}
            dvSourceStatuses={connectionStatuses}
            onConnectionSelectionChanged={props.handleConnectionSelectionChanged}
            selectedConnection={props.selectedConnection}
          />
        }
      />
    </>
  );
};
