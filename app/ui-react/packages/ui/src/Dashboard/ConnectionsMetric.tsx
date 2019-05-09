import { Text } from '@patternfly/react-core';
import { Card } from 'patternfly-react';
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
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Body className={'connections-metric__body'}>
          <Text>{this.props.i18nTitle}</Text>
        </Card.Body>
      </Card>
    );
  }
}
