import { useViewDefinitionDescriptors, useVirtualizationHelpers } from '@syndesis/api';
import { ImportSources, RestDataService, ViewDefinitionDescriptor, ViewInfo } from '@syndesis/models';
import { ViewsImportLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import resolvers from '../../../resolvers';
import { ViewInfosContent, ViewsImportSteps } from '../../shared';

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
  virtualization: RestDataService;
  connectionId: string;
}

export const SelectViewsPage: React.FunctionComponent = () => {
  const { params, state, history } = useRouteData<
    ISelectViewsRouteParams,
    ISelectViewsRouteState
  >();
  const [saveInProgress, setSaveInProgress] = React.useState(false);
  const [selectedViews, setSelectedViews] = React.useState<ViewInfo[]>([]);
  const [hasSelectedViews, setHasSelectedViews] = React.useState(false);
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { importSource } = useVirtualizationHelpers();

  const getExistingViewNames = (defnDescriptors: ViewDefinitionDescriptor[]) => {
    const viewNames: string[] = [];
    for (const descriptor of defnDescriptors) {
      viewNames.push(descriptor.name);
    }
    return viewNames;
  };

  const handleAddView = async (view: ViewInfo) => {
    const currentViews = selectedViews;
    currentViews.push(view);
    setSelectedViews(currentViews);
    setHasSelectedViews(currentViews.length > 0);
  };

  const handleRemoveView = async (viewName: string) => {
    const currentViews = selectedViews;
    const index = currentViews.findIndex(view => view.viewName === viewName);

    if (index !== -1) {
      currentViews.splice(index, 1);
    }
    setSelectedViews(currentViews);
    setHasSelectedViews(currentViews.length > 0);
  };

  const setInProgress = async (isWorking: boolean) => {
    setSaveInProgress(isWorking);
  };

  const virtualization = state.virtualization;
  const { resource: viewDefinitionDescriptors } = useViewDefinitionDescriptors(
    virtualization.keng__id
  );

  const handleCreateViews = async () => {
    setInProgress(true);
    const viewNames = selectedViews.map(selectedView => selectedView.viewName);
    const connName = selectedViews[0].connectionName;
    const importSources: ImportSources = {
      tables: viewNames,
    };

    try {
      await importSource(params.virtualizationId, connName, importSources);
      pushNotification(
        t('virtualization.importViewsSuccess', {
          name: virtualization.keng__id,
        }),
        'success'
      );
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('virtualization.importViewsFailed', {
          details,
          name: virtualization.keng__id,
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
    <ViewsImportLayout
      header={<ViewsImportSteps step={2} />}
      content={
        <ViewInfosContent
          connectionName={state.connectionId}
          existingViewNames={getExistingViewNames(viewDefinitionDescriptors)}
          onViewSelected={handleAddView}
          onViewDeselected={handleRemoveView}
        />
      }
      cancelHref={resolvers.data.virtualizations.views.root({
        virtualization,
      })}
      backHref={resolvers.data.virtualizations.views.importSource.selectConnection(
        { virtualization }
      )}
      onCreateViews={handleCreateViews}
      isNextDisabled={!hasSelectedViews}
      isNextLoading={saveInProgress}
      isLastStep={true}
    />
  );
};
