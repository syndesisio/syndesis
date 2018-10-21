import { Card, EmptyState } from 'patternfly-react';
import * as React from 'react';
import { IConnection } from '../../containers';

export interface IConnectionProps {
  connection: IConnection;
}

export class Connection extends React.Component<IConnectionProps> {
  public render() {
    return (
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Body>
          <EmptyState>
            <EmptyState.Icon/>
            <EmptyState.Title>
              {this.props.connection.name}
            </EmptyState.Title>
            <EmptyState.Info>
              {this.props.connection.description}
            </EmptyState.Info>
          </EmptyState>
        </Card.Body>
      </Card>
    );
  }
}