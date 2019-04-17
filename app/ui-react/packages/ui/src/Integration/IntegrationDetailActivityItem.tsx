import { Col, Icon, ListView, Row } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemProps {
  date: string;
  errorCount: number;
  i18nErrorsFound: string;
  i18nNoErrors: string;
  i18nNoSteps: string;
  i18nVersion: string;
  steps: JSX.Element[];
  time: string;
  version?: number;
}

export class IntegrationDetailActivityItem extends React.Component<
  IIntegrationDetailActivityItemProps
> {
  public render() {
    return (
      <ListView.Item
        key={1}
        actions={
          <>
            {this.props.errorCount > 0 ? (
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
        heading={this.props.date}
        description={this.props.time}
        additionalInfo={[
          <React.Fragment key={'not-really-needed'}>
            {this.props.i18nVersion}
            {'  '}
            {this.props.version}
          </React.Fragment>,
        ]}
      >
        <Row>
          {this.props.steps ? (
            <Col sm={11}>{this.props.steps}</Col>
          ) : (
            <Col sm={11}>
              <span>{this.props.i18nNoSteps}</span>
            </Col>
          )}
        </Row>
      </ListView.Item>
    );
  }
}
