import { Card, CardBody, Text } from '@patternfly/react-core';
import * as React from 'react';
import './ApiConnectorDetailCard.css';

export interface IApiConnectorDetailCardProps {
  description?: string;
  icon?: string;
  name: string;
}

export const ApiConnectorDetailCard: React.FunctionComponent<
  IApiConnectorDetailCardProps
> = (
  {
    description,
    icon,
    name
  }) => {
  return (
    <Card className="api-connector-card">
      <CardBody>
        <div className={'api-connector-card__content'}>
          <div>
            <img className="api-connector-card__icon" src={icon} />
          </div>
          <div
            className="api-connector__title h2"
            data-testid={'api-connector-detail-card-title'}
          >
            {name}
          </div>
          <Text className="api-connector-card__description">
            {description}
          </Text>
        </div>
      </CardBody>
    </Card>
  );
}
