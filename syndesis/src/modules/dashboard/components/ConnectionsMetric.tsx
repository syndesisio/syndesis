import { AggregateStatusCount, Card } from 'patternfly-react';
import * as React from 'react';

export interface IConnectionsMetricProps {
  count: number;
}

export class ConnectionsMetric extends React.PureComponent<
  IConnectionsMetricProps
> {
  public render() {
    return (
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Title>
          <AggregateStatusCount>{this.props.count}</AggregateStatusCount>{' '}
          Connections
        </Card.Title>
      </Card>
    );
  }
}
