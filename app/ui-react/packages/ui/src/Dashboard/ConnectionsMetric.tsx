import { Text } from '@patternfly/react-core';
import { Card, CardBody } from '@patternfly/react-core';
import * as React from 'react';
import './ConnectionsMetric.css';

export interface IConnectionsMetricProps {
  i18nTitle: string;
}

export class ConnectionsMetric extends React.PureComponent<
  IConnectionsMetricProps
> {
  public render() {
    return (
      <Card className="aggregate-status">
        <CardBody className={'connections-metric__body'}>
          <Text>{this.props.i18nTitle}</Text>
        </CardBody>
      </Card>
    );
  }
}
