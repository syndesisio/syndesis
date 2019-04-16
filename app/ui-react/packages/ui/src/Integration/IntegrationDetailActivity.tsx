import { Col, ListView, ListViewItem, Row } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemSteps {
  name: string;
  pattern: string;
}

export interface IIntegrationDetailActivityItem {
  date?: string;
  errorCount: number;
  steps?: IIntegrationDetailActivityItemSteps[];
  time?: string;
  version: number;
}

export interface IIntegrationDetailActivityProps {
  i18nNoErrors: string;
  i18nNoSteps: string;
  i18nVersion: string;
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
                        i.steps.map((step, idx) => (
                          <span key={idx}>{step.name}</span>
                        ))
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
