import {
  useConnections,
  useVirtualizationConnectionStatuses,
  useVirtualizationRuntimeMetadata,
} from '@syndesis/api';
import {
  SchemaNodeInfo,
  Virtualization,
} from '@syndesis/models';
import { CreateViewHeader, ViewCreateLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import resolvers from '../../../resolvers';
import { ConnectionSchemaContent, ConnectionTables } from '../../shared';

/**
 * @param virtualizationId - the ID of the virtualization for the wizard
 */
export interface ISelectSourcesRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 */
export interface ISelectSourcesRouteState {
  virtualization: Virtualization;
}

export interface ISelectSourcesPageProps {
  handleNodeSelected: (
    connectionName: string,
    name: string,
    teiidName: string,
    nodePath: string[]
  ) => void;
  handleNodeDeselected: (connectionName: string, teiidName: string) => void;
  selectedSchemaNodes: SchemaNodeInfo[];
}

export const SelectSourcesPage: React.FunctionComponent<ISelectSourcesPageProps> = props => {
  const { state } = useRouteData<null, ISelectSourcesRouteState>();
  const { t } = useTranslation(['data', 'shared']);
  const schemaNodeInfo: SchemaNodeInfo[] = props.selectedSchemaNodes;
  const virtualization = state.virtualization;

  const {
    resource: connectionStatuses,
    hasData: hasConnectionStatuses,
    error: connectionStatusesError,
  } = useVirtualizationConnectionStatuses();

  const {
    resource: connectionsData,
    hasData: hasConnectionsData,
    error: connectionsError,
  } = useConnections();

  const {
    resource: viewSourceInfo,
    hasData: hasViewSourceInfo,
    error: viewSourceInfoError,
  } = useVirtualizationRuntimeMetadata(virtualization.name);

  /**
   * Get error message based on the required fetches
   */
  const getErrorMessage = () => {
    if (connectionsError !== false) {
      return (connectionsError as Error).message;
    } else if (connectionStatusesError !== false) {
      return (connectionStatusesError as Error).message;
    } else if (viewSourceInfoError !== false) {
      return (viewSourceInfoError as Error).message;
    }
    return undefined;
  };

  return (
    <ViewCreateLayout
      header={
        <CreateViewHeader
          step={1}
          cancelHref={resolvers.data.virtualizations.views.root({
            virtualization,
          })}
          nextHref={resolvers.data.virtualizations.views.createView.selectName({
            schemaNodeInfo,
            virtualization,
          })}
          isNextDisabled={false}
          isNextLoading={false}
          isLastStep={false}
          i18nChooseTable={t('shared:ChooseTable')}
          i18nNameYourView={t('shared:NameYourView')}
          i18nBack={t('shared:Back')}
          i18nDone={t('shared:Done')}
          i18nNext={t('shared:Next')}
          i18nCancel={t('shared:Cancel')}
        />
      }
      content={
        <ConnectionSchemaContent
          error={connectionsError !== false || connectionStatusesError !== false || viewSourceInfoError !== false}
          errorMessage={getErrorMessage()}
          loading={!hasConnectionsData || !hasConnectionStatuses || !hasViewSourceInfo}
          dvSourceStatuses={connectionStatuses}
          connections={connectionsData.connectionsForDisplay}
          onNodeSelected={props.handleNodeSelected}
          onNodeDeselected={props.handleNodeDeselected}
          selectedSchemaNodes={props.selectedSchemaNodes}
        />
      }
      selectedTables={
        <ConnectionTables
          selectedSchemaNodes={props.selectedSchemaNodes}
          onNodeDeselected={props.handleNodeDeselected}
          columnDetails={viewSourceInfo.schemas}
        />
      }
    />
  );
};
