import {
  AggregateStatusCount,
  AggregateStatusNotification,
  AggregateStatusNotifications,
  Card,
  Icon
} from 'patternfly-react';
import * as React from 'react';

export interface IAggregatedMetricProps {
  title: string;
  ok: number;
  error: number;
}

export class AggregatedMetric extends React.PureComponent<IAggregatedMetricProps> {
  public render() {
    return (
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Title>
          <AggregateStatusCount>
            {this.props.title}
          </AggregateStatusCount>
        </Card.Title>
        <Card.Body>
          <AggregateStatusNotifications>
            <AggregateStatusNotification>
              <Icon type="pf" name="ok"/>
              {this.props.ok}
              {' '}
            </AggregateStatusNotification>
            <AggregateStatusNotification>
              <Icon type="pf" name="error-circle-o"/>
              {this.props.error}
            </AggregateStatusNotification>
          </AggregateStatusNotifications>
        </Card.Body>
      </Card>
    );
  }
}