// import * as H from 'history';
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
  OverlayTrigger,
  Row,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
// import { Link } from 'react-router-dom';

export class IntegrationDetailMetrics extends React.Component {
  public render() {
    return (
      <CardGrid>
        <Row style={{ marginBottom: '20px', marginTop: '20px' }}>
          <Col xs={6} sm={3} md={3}>
            <Card accented aggregated>
              <CardTitle>
                <a href="#">
                  <Icon name="shield" />
                  <AggregateStatusCount>9</AggregateStatusCount>
                  Ipsum
                </a>
              </CardTitle>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification>
                    <OverlayTrigger overlay={<Tooltip />}>
                      <a href="#" className="add">
                        <Icon type="pf" name="add-circle-o" />
                      </a>
                    </OverlayTrigger>
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </Col>
          <Col xs={6} sm={3} md={3}>
            <Card accented aggregated>
              <CardTitle>
                <a href="#">
                  <Icon name="shield" />
                  <AggregateStatusCount>20</AggregateStatusCount>
                  Amet
                </a>
              </CardTitle>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification>
                    <a href="#">
                      <Icon type="pf" name="error-circle-o" />4
                    </a>
                  </AggregateStatusNotification>
                  <AggregateStatusNotification>
                    <a href="#">
                      <Icon type="pf" name="warning-triangle-o" />1
                    </a>
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </Col>
          <Col xs={6} sm={3} md={3}>
            <Card accented aggregated>
              <CardTitle>
                <a href="#">
                  <Icon name="shield" />
                  <AggregateStatusCount>9</AggregateStatusCount>
                  Adipiscing
                </a>
              </CardTitle>
              <CardBody>
                <AggregateStatusNotifications>
                  <AggregateStatusNotification>
                    <Icon type="pf" name="ok" />
                  </AggregateStatusNotification>
                </AggregateStatusNotifications>
              </CardBody>
            </Card>
          </Col>
        </Row>
      </CardGrid>
    );
  }
}
