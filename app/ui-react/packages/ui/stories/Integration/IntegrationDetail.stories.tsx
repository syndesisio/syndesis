// import { number, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  // AggregatedMetricCard,
  Container,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailInfo,
  IntegrationDetailMetrics,
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
          <IntegrationDetailInfo name={'Integration name'} version={1} />
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
          <IntegrationDetailInfo name={'Integration name'} version={1} />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <p>Activity table goes here.</p>
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
          <IntegrationDetailInfo name={'Integration name'} version={1} />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <IntegrationDetailMetrics
          errorMessagesCount={26128}
          okMessagesCount={26126}
          lastProcessedDate={''}
          totalErrorsCount={2}
          totalMessages={2425}
          uptimeSince={'Apr 15th 15:37'}
        />
      </>
    </Router>
  ));
