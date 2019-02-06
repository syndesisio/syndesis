import { Card, CardBody } from 'patternfly-react';
import * as React from 'react';
import './ApiConnectorDetailCard.css';

export interface IApiConnectorDetailCardProps {
  description?: string;
  icon?: string;
  name: string;
}

export class ApiConnectorDetailCard extends React.Component<
  IApiConnectorDetailCardProps
> {
  public render() {
    return (
      <Card className="api-connector-card">
        <CardBody>
          <div className={'api-connector-card__content'}>
            <div className="api-connector-card__icon">
              <img src={this.props.icon} alt={this.props.name} />
            </div>
            <div
              className="api-connector__title h2"
              data-testid="api-connector-card-title"
            >
              {this.props.name}
            </div>
            <p className="api-connector-card__description">
              {this.props.description}
            </p>
          </div>
        </CardBody>
      </Card>
    );
  }
}
