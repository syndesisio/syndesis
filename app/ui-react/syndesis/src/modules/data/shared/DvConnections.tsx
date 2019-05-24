import { getConnectionIcon } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  DvConnectionCard,
  DvConnectionsGrid,
  DvConnectionsGridCell,
  DvConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { ApiError } from '../../../shared';
import {
  getDvConnectionStatus,
  isDvConnectionSelected,
} from './VirtualizationUtils';

export interface IDvConnectionsProps {
  error: boolean;
  loading: boolean;
  connections: Connection[];
  onConnectionSelectionChanged: (name: string, selected: boolean) => void;
}

export class DvConnections extends React.Component<IDvConnectionsProps> {
  public constructor(props: IDvConnectionsProps) {
    super(props);
    this.handleConnSourceSelectionChanged = this.handleConnSourceSelectionChanged.bind(
      this
    );
  }

  public handleConnSourceSelectionChanged(name: string, isSelected: boolean) {
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
          errorChildren={<ApiError />}
        >
          {() =>
            this.props.connections.map((c, index) => (
              <DvConnectionsGridCell key={index}>
                <DvConnectionCard
                  name={c.name}
                  description={c.description || ''}
                  dvStatus={getDvConnectionStatus(c)}
                  icon={getConnectionIcon(process.env.PUBLIC_URL, c)}
                  selected={isDvConnectionSelected(c)}
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
