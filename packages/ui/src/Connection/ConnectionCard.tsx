import * as H from 'history';
import { Card, EmptyState } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IConnectionProps {
  name: string;
  description: string;
  icon: string;
  href: H.LocationDescriptor;
}

export class ConnectionCard extends React.PureComponent<IConnectionProps> {
  public render() {
    return (
      <Link
        to={this.props.href}
        style={{
          color: 'inherit',
          textDecoration: 'none',
        }}
      >
        <Card matchHeight={true}>
          <Card.Body>
            <EmptyState>
              <div className="blank-slate-pf-icon">
                <img src={this.props.icon} alt={this.props.name} width={46} />
              </div>
              <EmptyState.Title>
                <span data-testid="connection-card-title">
                  {this.props.name}
                </span>
              </EmptyState.Title>
              <EmptyState.Info>{this.props.description}</EmptyState.Info>
            </EmptyState>
          </Card.Body>
        </Card>
      </Link>
    );
  }
}
