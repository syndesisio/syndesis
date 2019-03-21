// import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  Container,
  IntegrationDetailHistoryListView,
  IntegrationStepsHorizontalView,
  TabBar,
  TabBarItem,
} from '../../src';

const steps = [];

storiesOf('Integration/DetailTab', module).add('integration published', () => (
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

      <IntegrationDetailHistoryListView integrationIsDraft={false} />
    </>
  </Router>
));
