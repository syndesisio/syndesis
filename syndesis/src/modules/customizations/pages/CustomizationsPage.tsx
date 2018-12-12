import {
  Nav,
  NavItem,
  TabContainer,
  TabContent,
  TabPane,
} from 'patternfly-react';
import * as React from 'react';

export default class CustomizationsPage extends React.Component {
  public render() {
    return (
      <TabContainer id="basic-tabs" defaultActiveKey={1}>
        <div>
          <Nav bsClass="nav nav-tabs nav-tabs-pf">
            <NavItem eventKey={1} disabled={false}>
              {'API Client Connectors'}
            </NavItem>
            <NavItem eventKey={2} disabled={false}>
              {'Extensions'}
            </NavItem>
          </Nav>
          <TabContent animation={true}>
            <TabPane eventKey={1}>Tab 1 content</TabPane>
            <TabPane eventKey={2}>Tab 2 content</TabPane>
          </TabContent>
        </div>
      </TabContainer>
    );
  }
}
