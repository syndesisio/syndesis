import {
  Col,
  ListView,
  ListViewIcon,
  ListViewInfoItem,
  ListViewItem,
  Row,
} from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityProps {}

export class IntegrationDetailActivity extends React.Component<
  IIntegrationDetailActivityProps
> {
  public render() {
    return (
      <ListView>
        <ListViewItem
          actions={<div />}
          checkboxInput={<input />}
          leftContent={<ListViewIcon />}
          additionalInfo={[
            <ListViewInfoItem />,
            <ListViewInfoItem />,
            <ListViewInfoItem />,
          ]}
          heading="Item 1"
          description="This is Item 1 description"
        >
          <Row>
            <Col sm={11}>
              Lorem Ipsum is simply dummy text of the printing and typesetting
              industry
            </Col>
          </Row>
        </ListViewItem>
      </ListView>
    );
  }
}
