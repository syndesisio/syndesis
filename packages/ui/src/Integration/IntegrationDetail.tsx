import { Nav } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout';

// import { IntegrationDetailHistoryListView } from './IntegrationDetailHistoryListView';
// import { IntegrationDetailHistoryListViewItem } from './IntegrationDetailHistoryListViewItem';
// import { IntegrationDetailTab } from './IntegrationDetailTab';
import { IntegrationStepsHorizontalView } from './IntegrationStepsHorizontalView';

export interface IIntegrationDetailProps {
  // activity: JSX.Element;
  // details: JSX.Element;
  integrationName: string;
  // metrics: JSX.Element;
}

export class IntegrationDetail extends React.PureComponent<
  IIntegrationDetailProps
> {
  public render() {
    return (
      <Container>
        <div>
          <h1>{this.props.integrationName}</h1>
        </div>

        <IntegrationStepsHorizontalView />

        <Nav>Tabs go here..</Nav>
      </Container>
    );
  }
}
