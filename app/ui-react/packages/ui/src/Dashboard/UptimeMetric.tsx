import { Card } from 'patternfly-react';
import * as React from 'react';

export interface IUptimeMetricProps {
  start: number;
  i18nTitle: string;
}

export class UptimeMetric extends React.PureComponent<IUptimeMetricProps> {
  public render() {
    const startAsDate = new Date(this.props.start);
    const startAsHuman = startAsDate.toLocaleString();
    return (
      <Card accented={true} aggregated={true} matchHeight={true}>
        <Card.Title className={'text-left'}>
          <small className={'pull-right'}>since {startAsHuman}</small>
          <div>{this.props.i18nTitle}</div>
        </Card.Title>
        <Card.Body>TODO</Card.Body>
      </Card>
    );
  }
}
