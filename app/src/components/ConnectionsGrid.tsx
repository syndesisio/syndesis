import { IConnection } from '@syndesis/app/containers';
import { CardGrid } from 'patternfly-react';
import * as React from 'react';
import { Connection } from './Connection';
import { ConnectionSkeleton } from './ConnectionSkeleton';

export interface IConnectionsGridProps {
  loading: boolean;
  connections: IConnection[];
}

export class ConnectionsGrid extends React.Component<IConnectionsGridProps> {
  public render() {
    return (
      <CardGrid fluid={true} matchHeight={true}>
        <CardGrid.Row>
          {this.props.loading
            ? (new Array(5).fill(0)).map((_, index) =>
              <CardGrid.Col sm={6} md={3} key={index}>
                <ConnectionSkeleton key={index}/>
              </CardGrid.Col>
            )
            : this.props.connections.map((c, index) =>
              <CardGrid.Col sm={6} md={3} key={index}>
                <Connection connection={c}/>
              </CardGrid.Col>
            )
          }
        </CardGrid.Row>
      </CardGrid>
    );
  }
}