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

export interface IDvConnectionsState {
  selectedConnection: string;
}

export class DvConnections extends React.Component<
  IDvConnectionsProps,
  IDvConnectionsState
> {
  public constructor(props: IDvConnectionsProps) {
    super(props);
    this.state = {
      selectedConnection: this.props.initialSelection, // initial selection
    };
    this.handleConnSourceSelectionChanged = this.handleConnSourceSelectionChanged.bind(
      this
    );
  }

  public handleConnSourceSelectionChanged(name: string, isSelected: boolean) {
    const newSelection = isSelected ? name : '';
    this.setState({
      selectedConnection: newSelection,
    });
    this.props.onConnectionSelectionChanged(name, isSelected);
  }

  public render() {
    return (
      <DvConnectionsGrid>
        <WithLoader
          error={this.props.error}
          loading={this.props.loading}
          loaderChildren={
            <>
              {new Array(5).fill(0).map((_, index) => (
                <DvConnectionsGridCell key={index}>
                  <DvConnectionSkeleton />
                </DvConnectionsGridCell>
              ))}
            </>
          }
          errorChildren={<ApiError error={this.props.errorMessage!} />}
        >
          {() =>
            this.props.connections.map((c, index) => (
              <DvConnectionsGridCell key={index}>
                <DvConnectionCard
                  name={c.name}
                  description={c.description || ''}
                  dvStatus={getDvConnectionStatus(c)}
                  icon={<EntityIcon entity={c} alt={c.name} width={46} />}
                  selected={this.state.selectedConnection === c.name}
                  onSelectionChanged={this.handleConnSourceSelectionChanged}
                />
              </DvConnectionsGridCell>
            ))
          }
        </WithLoader>
      </DvConnectionsGrid>
    );
  }
}
