import { WithVirtualizationConnectionStatuses } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
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
  virtualization: RestDataService;
}

export const SelectConnectionPage: React.FunctionComponent = () => {

  const { state } = useRouteData<
    ISelectConnectionRouteParams,
    ISelectConnectionRouteState
  >();
  const [selectedConnection, setSelectedConnection] = React.useState('');

  const handleConnectionSelectionChanged = async (name: string, selected: boolean) => {
    const selConn = selected ? name : '';
    setSelectedConnection(selConn);
  }

  const connectionId = selectedConnection;
  const virtualization = state.virtualization;
  return (
    <ViewsImportLayout
      header={<ViewsImportSteps step={1} />}
      content={
        <WithVirtualizationConnectionStatuses>
          {({ data, hasData, error, errorMessage }) => (
            <DvConnectionsWithToolbar
              error={error}
              errorMessage={errorMessage}
              loading={!hasData}
              dvSourceStatuses={data}
              onConnectionSelectionChanged={
                handleConnectionSelectionChanged
              }
            />
          )}
        </WithVirtualizationConnectionStatuses>
      }
      cancelHref={resolvers.data.virtualizations.views.root({
        virtualization,
      })}
      nextHref={resolvers.data.virtualizations.views.importSource.selectViews(
        {
          connectionId,
          virtualization,
        }
      )}
      isNextDisabled={selectedConnection.length < 1}
      isNextLoading={false}
      isLastStep={false}
    />
  );

}
