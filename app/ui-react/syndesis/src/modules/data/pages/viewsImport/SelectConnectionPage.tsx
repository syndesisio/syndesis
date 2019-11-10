import { useVirtualizationConnectionStatuses } from '@syndesis/api';
import { Virtualization } from '@syndesis/models';
import { ViewsImportLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import resolvers from '../../../resolvers';
import { DvConnectionsWithToolbar, ViewsImportSteps } from '../../shared';

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

  const connectionId = props.selectedConnection;
  const virtualization = state.virtualization;
  const {
    resource: connectionStatuses,
    hasData: hasConnectionStatuses,
    error: connectionStatusesError,
  } = useVirtualizationConnectionStatuses();

  return (
    <ViewsImportLayout
      header={<ViewsImportSteps step={1} />}
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
    />
  );
};
