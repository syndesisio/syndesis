import {
  Button,
  Col,
  Icon,
  ListView,
  ListViewItem,
  Row,
} from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Container } from '../Layout';
import {
  IIntegrationDetailActivityItemStepsProps,
  IntegrationDetailActivityItemSteps,
} from './';

export interface IIntegrationDetailActivityItem {
  date?: string;
  errorCount: number;
  steps?: IIntegrationDetailActivityItemStepsProps[];
  time?: string;
  version: number;
}

export interface IIntegrationDetailActivityProps {
  i18nBtnRefresh: string;
  i18nErrorsFound: string;
  i18nLastRefresh: string;
  i18nNoErrors: string;
  i18nNoSteps: string;
  i18nVersion: string;
  i18nViewLogOpenShift: string;
  items: IIntegrationDetailActivityItem[];
  linkToOpenShiftLog: string;
}

export class IntegrationDetailActivity extends React.Component<
  IIntegrationDetailActivityProps
> {
  public render() {
    return (
      <>
        <Container>
          <div className="pull-right">
            <Link to={this.props.linkToOpenShiftLog}>
              {this.props.i18nViewLogOpenShift}
            </Link>
            {'  |  '}
            {this.props.i18nLastRefresh}
            {'  '}
            <Button>{this.props.i18nBtnRefresh}</Button>
          </div>
        </Container>
        <ListView>
          {this.props.items
            ? this.props.items.map((i, index) => (
                <ListViewItem
                  key={index}
                  actions={
                    <>
                      {i.errorCount > 0 ? (
                        <span>
                          <Icon type="pf" name="error-circle-o" />
                          {'  '}
                          {this.props.i18nErrorsFound}
                        </span>
                      ) : (
                        <span>
                          <Icon type="pf" name="ok" />
                          {'  '}
                          {this.props.i18nNoErrors}
                        </span>
                      )}
                    </>
                  }
                  heading={i.date}
                  description={i.time}
                  additionalInfo={
                    <>
                      {this.props.i18nVersion}
                      {'  '}
                      {i.version}
                    </>
                  }
                >
                  <Row>
                    <Col sm={11}>
                      {i.steps ? (
                        <IntegrationDetailActivityItemSteps>
                          {i.steps}
                        </IntegrationDetailActivityItemSteps>
                      ) : (
                        <span>{this.props.i18nNoSteps}</span>
                      )}
                    </Col>
                  </Row>
                </ListViewItem>
              ))
            : null}
        </ListView>
      </>
    );
  }
}
