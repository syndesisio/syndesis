import { Card, } from 'patternfly-react';
import * as React from 'react';

export interface IUptimeMetricProps {
  start: number;
}

export class UptimeMetric extends React.Component<IUptimeMetricProps> {
  public render() {
    const startAsDate = new Date(this.props.start);
    const startAsHuman = startAsDate.toLocaleString();
    return (
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Title>
          Uptime since {startAsHuman}
        </Card.Title>
        <Card.Body>
          TODO
        </Card.Body>
      </Card>
    );
  }
}