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

export interface IIntegrationDetailMetricsProps {
  errorMessagesCount?: number;
  okMessagesCount?: number;
  totalMessages?: number;
  uptimeStart: number;
}

export class IntegrationDetailMetrics extends React.Component<
  IIntegrationDetailMetricsProps
> {
  public render() {
    const startAsDate = new Date(this.props.uptimeStart);
    const startAsHuman = startAsDate.toLocaleString();

    return (
      <CardGrid>
        <Row style={{ marginBottom: '20px', marginTop: '20px' }}>
          <Col xs={6} sm={3} md={3}>
            <Card accented={true} aggregated={true} matchHeight={true}>
              <CardTitle>
                <Icon type="pf" name="error-circle-o" />
                {this.props.errorMessagesCount}
              </CardTitle>
              <CardBody>Total Errors</CardBody>
            </Card>
          </Col>
          <Col xs={6} sm={3} md={3}>
            <Card accented={true} aggregated={true} matchHeight={true}>
              <CardTitle>
                <Icon name="shield" />
                Last Processed
              </CardTitle>
              <CardBody>
                <h2>n/a</h2>
              </CardBody>
            </Card>
          </Col>
          <Col xs={6} sm={3} md={3}>
            <Card accented={true} aggregated={true} matchHeight={true}>
              <CardTitle>
                <AggregateStatusCount>
                  {this.props.totalMessages}&nbsp;
                </AggregateStatusCount>
                Total Messages
              </CardTitle>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification>
                    <Icon type="pf" name="ok" />
                    {this.props.okMessagesCount}&nbsp;
                  </AggregateStatusNotification>
                  <AggregateStatusNotification>
                    <Icon type="pf" name="error-circle-o" />
                    {this.props.errorMessagesCount}
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </Col>
          <Col xs={6} sm={3} md={3}>
            <Card accented={true} aggregated={true} matchHeight={true}>
              <Card.Title className={'text-left'}>
                <small className={'pull-right'}>Since {startAsHuman}</small>
                <div>Uptime</div>
              </Card.Title>
              <Card.Body>
                <></>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </CardGrid>
    );
  }
}
