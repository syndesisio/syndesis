import {
  AggregateStatusCount,
  AggregateStatusNotification,
  AggregateStatusNotifications,
  Card,
  CardBody,
  CardGrid,
  CardTitle,
  Col,
  Icon,
  Row,
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
  durationDifference?: string;
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
        <CardGrid fluid={true} matchHeight={true}>
          <Row style={{ marginBottom: '20px', marginTop: '20px' }}>
            <Col xs={6} sm={3} md={3}>
              <Card
                data-testid={'integration-detail-metrics-total-errors-card'}
                accented={true}
                aggregated={true}
                matchHeight={true}
              >
                <CardTitle>{this.props.i18nTotalErrors}</CardTitle>
                <CardBody>
                  <AggregateStatusNotifications>
                    <AggregateStatusNotification>
                      <Icon type="pf" name="error-circle-o" />
                      {this.props.errors ? this.props.errors : 0}
                    </AggregateStatusNotification>
                  </AggregateStatusNotifications>
                </CardBody>
              </Card>
            </Col>
            <Col xs={6} sm={3} md={3}>
              <Card
                data-testid={'integration-detail-metrics-last-processed-card'}
                accented={true}
                aggregated={true}
                matchHeight={true}
              >
                <CardTitle>{this.props.i18nLastProcessed}</CardTitle>
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
            </Col>
            <Col xs={6} sm={3} md={3}>
              <Card
                data-testid={'integration-detail-metrics-total-messages-card'}
                accented={true}
                aggregated={true}
                matchHeight={true}
              >
                <CardTitle>
                  <AggregateStatusCount>
                    {this.props.messages ? this.props.messages : 0}&nbsp;
                  </AggregateStatusCount>
                  {this.props.i18nTotalMessages}
                </CardTitle>
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
            </Col>
            <Col xs={6} sm={3} md={3}>
              <Card
                data-testid={'integration-detail-metrics-uptime-card'}
                accented={true}
                aggregated={true}
                matchHeight={true}
              >
                <Card.Title className="integration-detail-metrics__uptime-header">
                  <div>{this.props.i18nUptime}</div>
                  {this.props.start !== undefined &&
                    this.props.durationDifference !== undefined && (
                      <div className="integration-detail-metrics__uptime-uptime">
                        {startAsHuman}
                      </div>
                    )}
                </Card.Title>
                <Card.Body>
                  <AggregateStatusNotifications>
                    <AggregateStatusNotification className="integration-detail-metrics__duration-difference">
                      {this.props.durationDifference !== undefined
                        ? this.props.durationDifference
                        : this.props.i18nNoDataAvailable}
                    </AggregateStatusNotification>
                  </AggregateStatusNotifications>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </CardGrid>
      </PageSection>
    );
  }
}
