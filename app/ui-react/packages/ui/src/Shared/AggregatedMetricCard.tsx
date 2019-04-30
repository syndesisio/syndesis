import {
  AggregateStatusCount,
  AggregateStatusNotification,
  AggregateStatusNotifications,
  Card,
  Icon,
} from 'patternfly-react';
import * as React from 'react';

export interface IAggregatedMetricProps {
  title: string;
  ok: number;
  error: number;
}

export class AggregatedMetricCard extends React.PureComponent<
  IAggregatedMetricProps
> {
  public render() {
    return (
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Title>
          <AggregateStatusCount>
            <span data-testid="aggregate-title">{this.props.title}</span>
          </AggregateStatusCount>
        </Card.Title>
        <Card.Body>
          <AggregateStatusNotifications>
            <AggregateStatusNotification>
              <Icon type="pf" name="ok" />
              <span data-testid="aggregate-ok-count">{this.props.ok}</span>{' '}
            </AggregateStatusNotification>
            <AggregateStatusNotification>
              <Icon type="pf" name="error-circle-o" />
              <span data-testid="aggregate-error-count">
                {this.props.error}
              </span>
            </AggregateStatusNotification>
          </AggregateStatusNotifications>
        </Card.Body>
      </Card>
    );
  }
}
