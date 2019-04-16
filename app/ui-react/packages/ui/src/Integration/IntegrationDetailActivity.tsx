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
                  actions={<>{this.props.i18nNoErrors}</>}
                  heading={<span>{i.date}</span>}
                  description={
                    <>
                      {i.version}
                      {'  '}
                      {this.props.i18nVersion}
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
