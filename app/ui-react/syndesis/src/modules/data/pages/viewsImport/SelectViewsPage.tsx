import {
  useViewDefinitionDescriptors,
  useVirtualizationConnectionStatuses,
  useVirtualizationHelpers,
} from '@syndesis/api';
import {
  ImportSources,
  ViewDefinitionDescriptor,
  ViewInfo,
  Virtualization,
  VirtualizationSourceStatus,
} from '@syndesis/models';
import { ViewsImportLayout, ViewWizardHeader } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
import { DvConnectionStatus, ViewInfosContent } from '../../shared';

/**
 * @param virtualizationId - the ID of the virtualization for the wizard.
 */
export interface ISelectViewsRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 * @param connectionId - the id of the selected connection
 */
export interface ISelectViewsRouteState {
  virtualization: Virtualization;
  connectionId: string;
}

export interface ISelectViewsPageProps {
  selectedViews: ViewInfo[];
  handleAddView: (view: ViewInfo) => void;
  handleRemoveView: (viewName: string) => void;
  handleSelectAll: (isSelected: boolean, AllViewInfo: any[]) => void;
}

export const SelectViewsPage: React.FunctionComponent<
  ISelectViewsPageProps
> = props => {
  const { params, state, history } = useRouteData<
    ISelectViewsRouteParams,
    ISelectViewsRouteState
  >();
  const [saveInProgress, setSaveInProgress] = React.useState(false);
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data']);
  const { importSource } = useVirtualizationHelpers();

  const getExistingViewNames = (
    defnDescriptors: ViewDefinitionDescriptor[]
  ) => {
    const viewNames: string[] = [];
    for (const descriptor of defnDescriptors) {
      viewNames.push(descriptor.name);
    }
    return viewNames;
  };

  const setInProgress = async (isWorking: boolean) => {
    setSaveInProgress(isWorking);
  };

  const getConnectionLoading = (
    connectionName: string,
    sourceStatuses: VirtualizationSourceStatus[]
  ) => {
    const sourceStatus = sourceStatuses.find(status => status.sourceName === connectionName);
    let connLoading = false;
    if(sourceStatus) {
      connLoading = sourceStatus.loading;
    }
    return connLoading;
  };

  const getConnectionStatus = (
    connectionName: string,
    sourceStatuses: VirtualizationSourceStatus[]
  ) => {
    const sourceStatus = sourceStatuses.find(status => status.sourceName === connectionName);
    let resultStatus = '';
    if(sourceStatus) {
      switch (sourceStatus.schemaState) {
        case 'ACTIVE':
          resultStatus = DvConnectionStatus.ACTIVE;
          break;
        case 'FAILED':
          resultStatus = DvConnectionStatus.FAILED;
          break;
        case 'MISSING':
          resultStatus = DvConnectionStatus.INACTIVE;
          break;
        default:
          break;
      }
    }
    return resultStatus;
  };

  const getConnectionTeiidName = (
    connectionName: string,
    sourceStatuses: VirtualizationSourceStatus[]
  ) => {
    const sourceStatus = sourceStatuses.find(status => status.sourceName === connectionName);
    return sourceStatus ? sourceStatus.teiidName : '';
  };

  const getConnectionLastLoad = (
    connectionName: string,
    sourceStatuses: VirtualizationSourceStatus[]
  ) => {
    const sourceStatus = sourceStatuses.find(status => status.sourceName === connectionName);
    if(sourceStatus && sourceStatus.lastLoad) {
      return sourceStatus.lastLoad;
    }
    return 0;
  };

  const virtualization = state.virtualization;
  const { resource: viewDefinitionDescriptors } = useViewDefinitionDescriptors(
    virtualization.name
  );

  const {
    resource: connectionStatuses,
  } = useVirtualizationConnectionStatuses();

  const handleCreateViews = async () => {
    setInProgress(true);
    const viewNames = props.selectedViews.map(
      selectedView => selectedView.viewName
    );
    const connName = props.selectedViews[0].connectionName;
    const importSources: ImportSources = {
      tables: viewNames,
    };

    try {
      await importSource(params.virtualizationId, connName, importSources);
      pushNotification(
        t('importViewsSuccess', {
          name: virtualization.name,
        }),
        'success'
      );
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('importViewsFailed', {
          details,
          name: virtualization.name,
        }),
        'error'
      );
    }
    setInProgress(false);
    history.push(
      resolvers.data.virtualizations.views.root({
        virtualization,
      })
    );
  };

  return (
    <>
      <PageTitle title={t('importViewsPageTitle')} />
      <ViewsImportLayout
        header={
          <ViewWizardHeader
            step={2}
            cancelHref={resolvers.data.virtualizations.views.root({
              virtualization,
            })}
            backHref={resolvers.data.virtualizations.views.importSource.selectConnection(
              { virtualization }
            )}
            onNext={handleCreateViews}
            isNextDisabled={props.selectedViews.length < 1}
            isNextLoading={saveInProgress}
            isLastStep={true}
            i18nStep1Text={t('data:importDataSourceWizardStep1')}
            i18nStep2Text={t('data:importDataSourceWizardStep2')}
            i18nBack={t('shared:Back')}
            i18nDone={t('shared:Done')}
            i18nNext={t('shared:Next')}
            i18nCancel={t('shared:Cancel')}
          />
        }
        content={
          <ViewInfosContent
            connectionLoading={getConnectionLoading(state.connectionId, connectionStatuses)}
            connectionName={state.connectionId}
            connectionStatus={getConnectionStatus(state.connectionId, connectionStatuses)}
            connectionStatusMessage={''}
            connectionTeiidName={getConnectionTeiidName(state.connectionId, connectionStatuses)}
            existingViewNames={getExistingViewNames(viewDefinitionDescriptors)}
            connectionLastLoad={getConnectionLastLoad(state.connectionId, connectionStatuses)}
            onViewSelected={props.handleAddView}
            onViewDeselected={props.handleRemoveView}
            selectedViews={props.selectedViews}
            handleSelectAll={props.handleSelectAll}
          />
        }
      />
    </>
  );
};
