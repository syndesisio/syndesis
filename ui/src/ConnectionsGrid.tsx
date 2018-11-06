import { CardGrid } from 'patternfly-react';
import * as React from 'react';
import { ConnectionSkeleton } from './ConnectionSkeleton';

export interface IConnectionsGridProps {
  loading: boolean;
  children: JSX.Element[]
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
            : this.props.children.map((c: any, index: number) =>
              <CardGrid.Col sm={6} md={3} key={index}>
                {c}
              </CardGrid.Col>
            )
          }
        </CardGrid.Row>
      </CardGrid>
    );
  }
}