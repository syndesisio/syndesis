import { Card } from 'patternfly-react';
import * as React from 'react';

export interface IConnectionsMetricProps {
  count: number;
  i18nTitle: string;
}

export class ConnectionsMetric extends React.PureComponent<
  IConnectionsMetricProps
> {
  public render() {
    return (
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Title>{this.props.i18nTitle}</Card.Title>
      </Card>
    );
  }
}
