import { Nav } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout';
import { IntegrationStepsHorizontalView } from './IntegrationStepsHorizontalView';

export interface IntegrationDetailProps {
  activity: JSX.Element;
  details: JSX.Element;
  integrationName: string;
  metrics: JSX.Element;
}

export class IntegrationDetail extends React.PureComponent {
  public render() {
    return (
      <Container>
        <div>
          <h1>IntegrationName</h1>
        </div>

        <IntegrationStepsHorizontalView />

        <Nav>Tabs go here..</Nav>
      </Container>
    );
  }
}
