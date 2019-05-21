import { Text } from '@patternfly/react-core';
import { Card, CardBody } from 'patternfly-react';
import * as React from 'react';
import { toTestId } from '../utils';
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
            <div>
              <img className="api-connector-card__icon" src={this.props.icon} />
            </div>
            <div
              className="api-connector__title h2"
              data-testid={`${toTestId(ApiConnectorDetailCard.name, 'title')}`}
            >
              {this.props.name}
            </div>
            <Text className="api-connector-card__description">
              {this.props.description}
            </Text>
          </div>
        </CardBody>
      </Card>
    );
  }
}
