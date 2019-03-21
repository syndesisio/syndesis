// import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  Container,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationStepsHorizontalView,
  TabBar,
  TabBarItem,
} from '../../src';

const integrationPublishedHistoryItems = [
  <IntegrationDetailHistoryListViewItem
    key={0}
    integrationUpdatedAt={'Feb 24, 2019, 04:27:49'}
    integrationVersion={1}
    i18nTextHistoryMenuReplaceDraft={'Replace draft'}
    i18nTextHistoryMenuUnpublish={'Unpublish'}
    i18nTextLastPublished={'Last published on '}
    i18nTextVersion={'Version'}
  />,
];

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
        <IntegrationDetailHistoryListView
          integrationIsDraft={false}
          children={integrationPublishedHistoryItems}
          i18nTextDraft={'Draft'}
          i18nTextHistory={'History'}
        />
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
