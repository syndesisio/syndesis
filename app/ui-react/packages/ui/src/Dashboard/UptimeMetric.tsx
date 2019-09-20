import { Card, CardBody, Title } from '@patternfly/react-core';
import * as React from 'react';
import './UptimeMetric.css';

export interface IUptimeMetricProps {
  start: number;
  uptimeDuration: string;
  i18nTitle: string;
}

export class UptimeMetric extends React.PureComponent<IUptimeMetricProps> {
  public render() {
    const startAsDate = new Date(this.props.start);
    const startAsHuman = startAsDate.toLocaleString();
    return (
      <Card className="metrics-uptime aggregate-status">
        <Title size="md" className="metrics-uptime__header">
          <div>{this.props.i18nTitle}</div>
          <div className="metrics-uptime__uptime">since {startAsHuman}</div>
        </Title>
        <CardBody>
          <span>{this.props.uptimeDuration}</span>
        </CardBody>
      </Card>
    );
  }
}
