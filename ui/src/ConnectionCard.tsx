import { Card, EmptyState } from 'patternfly-react';
import * as React from 'react';

export interface IConnectionProps {
  name: string;
  description: string;
  icon: string;
}

export class ConnectionCard extends React.PureComponent<IConnectionProps> {
  public render() {
    return (
      <Card matchHeight={true}>
        <Card.Body>
          <EmptyState>
            <div className="blank-slate-pf-icon">
              <img src={this.props.icon} alt={this.props.name} width={46}/>
            </div>
            <EmptyState.Title>
              {this.props.name}
            </EmptyState.Title>
            <EmptyState.Info>
              {this.props.description}
            </EmptyState.Info>
          </EmptyState>
        </Card.Body>
      </Card>
    );
  }
}