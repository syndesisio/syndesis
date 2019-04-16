import { Col, ListView, ListViewItem, Row } from 'patternfly-react';
import * as React from 'react';
import {
  // IntegrationDetailActivityItemSteps,
  IIntegrationDetailActivityItemStepsProps,
} from './';

export interface IIntegrationDetailActivityItem {
  date?: string;
  errorCount: number;
  steps?: IIntegrationDetailActivityItemStepsProps[];
  time?: string;
  version: number;
}

export interface IIntegrationDetailActivityProps {
  i18nErrorsFound: string;
  i18nLastRefresh: string;
  i18nNoErrors: string;
  i18nNoSteps: string;
  i18nRefresh: string;
  i18nVersion: string;
  i18nViewLogOpenShift: string;
  items: IIntegrationDetailActivityItem[];
}

export class IntegrationDetailActivity extends React.Component<
  IIntegrationDetailActivityProps
> {
  public render() {
    return (
      <>
        <ListView>
          {this.props.items
            ? this.props.items.map((i, index) => (
                <ListViewItem
                  key={index}
                  actions={
                    <>
                      {i.errorCount > 0 ? (
                        <span>{this.props.i18nErrorsFound}</span>
                      ) : (
                        <span>{this.props.i18nNoErrors}</span>
                      )}
                    </>
                  }
                  heading={<span>{i.date}</span>}
                  description={
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
                        <>Activity Item Steps will go here when ready.</>
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
