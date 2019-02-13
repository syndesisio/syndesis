import { Connection } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { getConnectionIcon, WithLoader } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';

export interface IConnectionsProps {
  error: boolean;
  loading: boolean;
  connections: Connection[];
  getConnectionHref(connection: Connection): H.LocationDescriptor;
}

export class Connections extends React.Component<IConnectionsProps> {
  public render() {
    return (
      <ConnectionsGrid>
        <WithLoader
          error={this.props.error}
          loading={this.props.loading}
          loaderChildren={
            <>
              {new Array(5).fill(0).map((_, index) => (
                <ConnectionsGridCell key={index}>
                  <ConnectionSkeleton />
                </ConnectionsGridCell>
              ))}
            </>
          }
          errorChildren={<div>TODO</div>}
        >
          {() =>
            this.props.connections.map((c, index) => (
              <ConnectionsGridCell key={index}>
                <ConnectionCard
                  name={c.name}
                  description={c.description || ''}
                  icon={getConnectionIcon(c, process.env.PUBLIC_URL)}
                  href={this.props.getConnectionHref(c)}
                />
              </ConnectionsGridCell>
            ))
          }
        </WithLoader>
      </ConnectionsGrid>
    );
  }
}
