import { Col, ListView, ListViewItem, Row } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItem {
  date: string;
  errorCount: number;
  time: string;
  version: number;
}

export interface IIntegrationDetailActivityProps {
  i18nNoErrors: string;
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
                      Lorem Ipsum is simply dummy text of the printing and
                      typesetting industry
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
