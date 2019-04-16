// import { number, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  Container,
  IntegrationDetailActivity,
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

const activityItemsSteps1 = [
  {
    duration: 4,
    i18nHeaderDuration: 'Duration',
    i18nHeaderDurationUnit: 'ms',
    i18nHeaderOutput: 'Output',
    i18nHeaderStatus: 'Status',
    i18nHeaderStep: 'Step',
    i18nHeaderTime: 'Time',
    i18nNoOutput: 'No output',
    name: 'Data Mapper',
    output: 'No output',
    status: 'Success',
    time: 'Mar 14, 2019, 14:24:29',
  },
  {
    duration: 11,
    i18nHeaderDuration: 'Duration',
    i18nHeaderDurationUnit: 'ms',
    i18nHeaderOutput: 'Output',
    i18nHeaderStatus: 'Status',
    i18nHeaderStep: 'Step',
    i18nHeaderTime: 'Time',
    i18nNoOutput: 'No output',
    name: 'Invoke stored procedure',
    output: 'No output',
    status: 'Success',
    time: 'Mar 14, 2019, 14:24:29',
  },
];

const activityItemsSteps2 = [
  {
    duration: 13,
    i18nHeaderDuration: 'Duration',
    i18nHeaderDurationUnit: 'ms',
    i18nHeaderOutput: 'Output',
    i18nHeaderStatus: 'Status',
    i18nHeaderStep: 'Step',
    i18nHeaderTime: 'Time',
    name: 'Data Mapper',
    output:
      '<pre>io.atlasmap.api.AtlasException: java.lang.IllegalArgumentException: document cannot be null nor empty</pre>',
    status: 'Error',
    time: 'Mar 14, 2019, 14:23:35',
  },
];

const activityItems = [
  {
    date: '4/16/2019',
    errorCount: 4,
    steps: activityItemsSteps1,
    time: '07:40:28',
    version: 2,
  },
  {
    date: '4/16/2019',
    errorCount: 0,
    steps: activityItemsSteps2,
    time: '07:40:28',
    version: 2,
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
        <IntegrationDetailActivity
          i18nBtnDetails={'Refresh'}
          i18nErrorsFound={'Errors found'}
          i18nLastRefresh={'Last refresh'}
          i18nNoErrors={'No errors'}
          i18nRefresh={'Refresh'}
          i18nNoSteps={'No steps information was found for this integration'}
          i18nVersion={'Version'}
          i18nViewLogOpenShift={'View Log in OpenShift'}
          items={activityItems}
        />
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
          errorMessagesCount={2}
          okMessagesCount={2425}
          lastProcessedDate={''}
          totalErrorsCount={2}
          totalMessages={26126}
          uptimeStart={5358454957349}
        />
      </>
    </Router>
  ));
