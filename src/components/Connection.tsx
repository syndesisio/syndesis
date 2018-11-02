import { IConnection } from '@syndesis/ui/containers';
import { Card, EmptyState } from 'patternfly-react';
import * as React from 'react';

export interface IConnectionProps {
  connection: IConnection;
}

export class Connection extends React.Component<IConnectionProps> {
  public render() {
    const iconSrc = this.props.connection.icon.startsWith('data:')
      ? this.props.connection.icon
      : `${process.env.PUBLIC_URL}/icons/${this.props.connection.id}.connection.png`;
    return (
      <Card matchHeight={true}>
        <Card.Body>
          <EmptyState>
            <div className="blank-slate-pf-icon">
              <img src={iconSrc} alt={this.props.connection.name} width={46}/>
            </div>
            <EmptyState.Title>
              {this.props.connection.name}
            </EmptyState.Title>
            <EmptyState.Info>
              {this.props.connection.description || ''}
            </EmptyState.Info>
          </EmptyState>
        </Card.Body>
      </Card>
    );
  }
}