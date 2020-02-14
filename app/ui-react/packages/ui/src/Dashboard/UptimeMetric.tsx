import { Card, CardBody, Title } from '@patternfly/react-core';
import * as React from 'react';
import './UptimeMetric.css';

export interface IUptimeMetricProps {
  start: number;
  uptimeDuration: string;
  i18nTitle: string;
}

export const UptimeMetric: React.FunctionComponent<IUptimeMetricProps> = ({
  i18nTitle,
  start,
  uptimeDuration,
}) => {
  const startAsDate = new Date(start);
  const startAsHuman = startAsDate.toLocaleString();
  return (
    <Card
      data-testid={'dashboard-page-metrics-uptime'}
      className="metrics-uptime aggregate-status"
    >
      <CardBody>
        <Title size="md" className="metrics-uptime__header">
          <div>{i18nTitle}</div>
          <div className="metrics-uptime__uptime">since {startAsHuman}</div>
        </Title>
      </CardBody>
      <CardBody>
        <span>{uptimeDuration}</span>
      </CardBody>
    </Card>
  );
};
