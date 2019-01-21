import * as H from 'history';
import { Card } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

import './ConnectionCard.css';

export interface IConnectionProps {
  name: string;
  description: string;
  icon: string;
  href: H.LocationDescriptor;
}

export class ConnectionCard extends React.PureComponent<IConnectionProps> {
  public render() {
    return (
      <Link to={this.props.href} className={'connection-card'}>
        <Card matchHeight={true}>
          <Card.Body>
            <div className={'connection-card__content'}>
              <div className="connection-card__icon">
                <img src={this.props.icon} alt={this.props.name} width={46} />
              </div>
              <div
                className="connection-card__title h2"
                data-testid="connection-card-title"
              >
                {this.props.name}
              </div>
              <p className="connection-card__description">
                {this.props.description}
              </p>
            </div>
          </Card.Body>
        </Card>
      </Link>
    );
  }
}
