import { Connection } from '@syndesis/models';
import {
  DvConnectionCard,
  DvConnectionsGrid,
  DvConnectionsGridCell,
} from '@syndesis/ui';
import * as React from 'react';
import { EntityIcon } from '../../../shared';
import {
  getDvConnectionStatus,
  getDvConnectionStatusMessage,
  isDvConnectionLoading,
} from './VirtualizationUtils';

export interface IDvConnectionsProps {
  connections: Connection[];
  initialSelection: string; // Name of initially selected connection
  onConnectionSelectionChanged: (name: string, selected: boolean) => void;
}

export const DvConnections: React.FunctionComponent<
  IDvConnectionsProps
> = props => {
  const handleConnSourceSelectionChanged = (
    name: string,
    isSelected: boolean
  ) => {
    props.onConnectionSelectionChanged(name, isSelected);
  };

  return (
    <DvConnectionsGrid>
      {props.connections.map((c, index) => (
        <DvConnectionsGridCell key={index}>
          <DvConnectionCard
            name={c.name}
            description={c.description || ''}
            dvStatus={getDvConnectionStatus(c)}
            dvStatusTooltip={getDvConnectionStatusMessage(c)}
            icon={<EntityIcon entity={c} alt={c.name} width={46} />}
            loading={isDvConnectionLoading(c)}
            selected={props.initialSelection === c.name}
            onSelectionChanged={handleConnSourceSelectionChanged}
          />
        </DvConnectionsGridCell>
      ))}
    </DvConnectionsGrid>
  );
};
