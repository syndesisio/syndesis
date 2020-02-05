import { Card, CardBody, Title } from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
import {
  AggregateStatusCount,
  AggregateStatusNotification,
  AggregateStatusNotifications,
} from 'patternfly-react';
import * as React from 'react';

export interface IAggregatedMetricProps {
  title: string;
  ok: number;
  error: number;
  total: number;
}

export class AggregatedMetricCard extends React.PureComponent<
  IAggregatedMetricProps
> {
  public formatNumber(num: number) {
    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,');
  }

  public render() {
    return (
      <Card className="aggregate-status">
        <Title size="md">
          <AggregateStatusCount>
            <span data-testid={'aggregated-metric-card-total-count'}>
              {this.formatNumber(this.props.total)}
            </span>
            <span data-testid={'aggregated-metric-card-title'}>
              {' '}
              {this.props.title}
            </span>
          </AggregateStatusCount>
        </Title>
        <CardBody>
          <AggregateStatusNotifications>
            <AggregateStatusNotification>
              <OkIcon />
              <span data-testid={'aggregated-metric-card-ok-count'}>
                {this.formatNumber(this.props.ok)}
              </span>{' '}
            </AggregateStatusNotification>
            <AggregateStatusNotification>
              <ErrorCircleOIcon />
              <span data-testid={'aggregated-metric-card-error-count'}>
                {this.formatNumber(this.props.error)}
              </span>
            </AggregateStatusNotification>
          </AggregateStatusNotifications>
        </CardBody>
      </Card>
    );
  }
}
