import { Col, Icon, ListViewItem, Row } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemProps {
  date: string;
  errorCount: number;
  i18nErrorsFound: string;
  i18nNoErrors: string;
  i18nNoSteps: string;
  i18nVersion: string;
  time: string;
  version?: number;
}

export class IntegrationDetailActivityItem extends React.Component<
  IIntegrationDetailActivityItemProps
> {
  public render() {
    return (
      <ListViewItem
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
        additionalInfo={
          <>
            {this.props.i18nVersion}
            {'  '}
            {this.props.version}
          </>
        }
      >
        <Row>
          <Col sm={11}>
            {this.props.children ? (
              <span>Steps will be here soon.</span>
            ) : (
              <span>{this.props.i18nNoSteps}</span>
            )}
          </Col>
        </Row>
      </ListViewItem>
    );
  }
}
