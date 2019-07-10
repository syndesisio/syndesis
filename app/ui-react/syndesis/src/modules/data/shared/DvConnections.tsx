import { Connection } from '@syndesis/models';
import {
  DvConnectionCard,
  DvConnectionsGrid,
  DvConnectionsGridCell,
  DvConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, EntityIcon } from '../../../shared';
import { getDvConnectionStatus } from './VirtualizationUtils';

export interface IDvConnectionsProps {
  error: boolean;
  errorMessage?: string;
  loading: boolean;
  connections: Connection[];
  initialSelection: string; // Name of initially selected connection
  onConnectionSelectionChanged: (name: string, selected: boolean) => void;
}

export const DvConnections: React.FunctionComponent<
  IDvConnectionsProps
> = props => {

  const [selectedConnection, setSelectedConnection] = React.useState(props.initialSelection);

  const handleConnSourceSelectionChanged = (name: string, isSelected: boolean) => {
    const newSelection = isSelected ? name : '';
    setSelectedConnection(newSelection);
    
    props.onConnectionSelectionChanged(name, isSelected);
  }

    return (
      <DvConnectionsGrid>
        <WithLoader
          error={props.error}
          loading={props.loading}
          loaderChildren={
            <>
              {new Array(5).fill(0).map((_, index) => (
                <DvConnectionsGridCell key={index}>
                  <DvConnectionSkeleton />
                </DvConnectionsGridCell>
              ))}
            </>
          }
          errorChildren={<ApiError error={props.errorMessage!} />}
        >
          {() =>
            props.connections.map((c, index) => (
              <DvConnectionsGridCell key={index}>
                <DvConnectionCard
                  name={c.name}
                  description={c.description || ''}
                  dvStatus={getDvConnectionStatus(c)}
                  icon={<EntityIcon entity={c} alt={c.name} width={46} />}
                  selected={selectedConnection === c.name}
                  onSelectionChanged={handleConnSourceSelectionChanged}
                />
              </DvConnectionsGridCell>
            ))
          }
        </WithLoader>
      </DvConnectionsGrid>
    );
}
