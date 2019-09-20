import {
  Card,
  CardBody,
  CardHeader,
  Grid,
  GridItem,
  Title
} from '@patternfly/react-core';
import {
  AggregateStatusCount,
  AggregateStatusNotification,
  AggregateStatusNotifications,
  Icon,
} from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../../Layout';
import './IntegrationDetailMetrics.css';

export interface IIntegrationDetailMetricsProps {
  i18nLastProcessed: string;
  i18nNoDataAvailable: string;
  i18nSince: string;
  i18nTotalErrors: string;
  i18nTotalMessages: string;
  i18nUptime: string;
  errors?: number;
  lastProcessed?: string;
  messages?: number;
  start?: number;
  uptimeDuration?: string;
}

export class IntegrationDetailMetrics extends React.Component<
  IIntegrationDetailMetricsProps
  > {
  public render() {
    const okMessagesCount = this.props.messages! - this.props.errors!;
    const startAsDate = new Date(this.props.start!);
    const startAsHuman = this.props.i18nSince + startAsDate.toLocaleString();
    return (
      <PageSection className="integration-detail-metrics">

        <Grid md={6} xl={3} gutter={'sm'}>
          <GridItem>
            <Card data-testid={'integration-detail-metrics-total-errors-card'}>
              <CardHeader>
                <Title size="lg">{this.props.i18nTotalErrors}</Title>
              </CardHeader>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification>
                    <Icon type="pf" name="error-circle-o" />
                    {this.props.errors ? this.props.errors : 0}
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </GridItem>
          <GridItem>
            <Card data-testid={'integration-detail-metrics-last-processed-card'}>
              <CardHeader>
                <Title size="lg">{this.props.i18nLastProcessed}</Title>
              </CardHeader>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification className="integration-detail-metrics__last-processed">
                    {this.props.lastProcessed
                      ? this.props.lastProcessed
                      : this.props.i18nNoDataAvailable}
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </GridItem>
          <GridItem>
            <Card data-testid={'integration-detail-metrics-total-messages-card'}>
              <CardHeader>
                <Title size="lg">
                  <AggregateStatusCount>
                    {this.props.messages ? this.props.messages : 0}&nbsp;
                  </AggregateStatusCount>
                  {this.props.i18nTotalMessages}
                </Title>
              </CardHeader>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification>
                    <Icon type="pf" name="ok" />
                    {this.props.errors !== undefined &&
                      this.props.messages !== undefined
                      ? okMessagesCount
                      : 0}
                    &nbsp;
                    </AggregateStatusNotification>
                  <AggregateStatusNotification>
                    <Icon type="pf" name="error-circle-o" />
                    {this.props.errors ? this.props.errors : 0}
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </GridItem>
          <GridItem>
            <Card data-testid={'integration-detail-metrics-uptime-card'}>
              <CardHeader>
                <Title size="lg" className="integration-detail-metrics__uptime-header">
                  <div>{this.props.i18nUptime}</div>
                  {this.props.start !== undefined &&
                    this.props.uptimeDuration !== undefined && (
                      <div className="integration-detail-metrics__uptime-uptime">
                        {startAsHuman}
                      </div>
                    )}
                </Title>
              </CardHeader>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification className="integration-detail-metrics__duration-difference">
                    {this.props.uptimeDuration !== undefined
                      ? this.props.uptimeDuration
                      : this.props.i18nNoDataAvailable}
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </GridItem>
        </Grid>
      </PageSection>
    );
  }
}
