// import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  Container,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationStepsHorizontalView,
  TabBar,
  TabBarItem,
} from '../../src';

const steps = [
  {
    name: 'SQL',
    pattern: 'From',
  },
  {
    name: 'Salesforce',
    pattern: 'To',
  },
];

storiesOf('Integration/Detail', module)
  .add('Details Tab Page', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <IntegrationStepsHorizontalView steps={steps} />
        <IntegrationDetailDescription description={'This is my description.'} />
        <IntegrationDetailHistoryListView integrationIsDraft={false} />
      </>
    </Router>
  ))
  .add('Activity Tab Page', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <p>Activity Tab</p>
      </>
    </Router>
  ))
  .add('Metrics Tab Page', () => (
    <Router>
      <>
        <Container
          style={{
            background: '#fff',
          }}
        >
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <p>Metrics Tab</p>
      </>
    </Router>
  ));
