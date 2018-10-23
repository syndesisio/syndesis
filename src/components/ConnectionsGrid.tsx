import { CardGrid } from 'patternfly-react';
import * as React from 'react';
import { IConnection } from '../containers';
import { Connection } from './Connection';

export interface IConnectionsGridProps {
  connections: IConnection[];
}

export class ConnectionsGrid extends React.Component<IConnectionsGridProps> {
  public render() {
    return (
      <CardGrid fluid={true} matchHeight={true}>
        <CardGrid.Row>
          {this.props.connections.map((c, index) =>
            <CardGrid.Col sm={6} md={3} key={index}>
              <Connection connection={c}/>
            </CardGrid.Col>
          )}
        </CardGrid.Row>
      </CardGrid>
    );
  }
}