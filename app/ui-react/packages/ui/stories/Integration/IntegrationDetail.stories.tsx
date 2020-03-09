import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import {
  Container,
  IntegrationDetailActivity,
  IntegrationDetailActivityItem,
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
  IntegrationDetailInfo,
  IntegrationDetailMetrics,
  IntegrationStepsHorizontalItem,
  IntegrationStepsHorizontalView,
  TabBar,
  TabBarItem,
} from '../../src';

const datePublished = Date.parse('24 Feb 2019 08:19:42 GMT');

const activityItemsSteps1 = [
  {
    name: 'Data Mapper',
    time: 'Mar 14, 2019, 14:24:29',
    duration: '4',
    output: 'No output',
    status: 'Success'
  },
  {
    name: 'Invoke stored procedure',
    timee: 'Mar 14, 2019, 14:24:29',
    duration: '4',
    output: 'No output',
    status: 'Success'
  }
];

const activityItemsSteps2 = [
  {
    name: 'Invoke stored procedure',
    time: 'Mar 14, 2019, 14:23:35',
    duration: '67',
    output: 'io.atlasmap.api.AtlasException: java.lang.IllegalArgumentException: document cannot be null nor empty',
    status: 'Error'
  }
];

const activityItems = [
  <IntegrationDetailActivityItem
    steps={activityItemsSteps1}
    date={'4/16/2019'}
    errorCount={0}
    i18nError={'Error'}
    i18nErrorsFound={'Errors found'}
    i18nHeaderDuration={'Duration'}
    i18nHeaderOutput={'Output'}
    i18nHeaderStatus={'Status'}
    i18nHeaderStep={'Step'}
    i18nHeaderTime={'Time'}
    i18nNoErrors={'No errors'}
    i18nNoOutput={'No output'}
    i18nNoSteps={'No steps information was found for this integration'}
    i18nSuccess={'Success'}
    i18nVersion={'Version'}
    key={0}
    time={'07:40:28'}
    version={'2'}
  />,
  <IntegrationDetailActivityItem
    steps={activityItemsSteps2}
    date={'4/14/2019'}
    errorCount={5}
    i18nError={'Error'}
    i18nErrorsFound={'Errors found'}
    i18nHeaderDuration={'Duration'}
    i18nHeaderOutput={'Output'}
    i18nHeaderStatus={'Status'}
    i18nHeaderStep={'Step'}
    i18nHeaderTime={'Time'}
    i18nNoErrors={'No errors'}
    i18nNoOutput={'No output'}
    i18nNoSteps={'No steps information was found for this integration'}
    i18nSuccess={'Success'}
    i18nVersion={'Version'}
    key={1}
    time={'07:40:28'}
    version={'2'}
  />,
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
          <IntegrationDetailInfo
            name={'Integration name'}
            version={1}
            currentState={'Published'}
            i18nLogUrlText={'log url'}
            i18nProgressPending={'progress pending'}
            i18nProgressStarting={'progress starting'}
            i18nProgressStopping={'progress stopping'}
            targetState={'Published'}
          />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <IntegrationStepsHorizontalView
          children={[
            {
              name: 'Fhir Test',
            },
            {
              name: 'Log',
            },
          ].map((s, idx) => (
            <IntegrationStepsHorizontalItem
              key={idx}
              name={s.name}
              icon={<div />}
            />
          ))}
        />
        <IntegrationDetailDescription description={'This is my description.'} />
        <IntegrationDetailHistoryListView
          hasHistory={true}
          isDraft={false}
          i18nTextDraft={'Draft'}
          i18nTextHistory={'History'}
          children={[
            {
              actions: {},
              updatedAt: datePublished,
              version: 2,
            },
            {
              actions: {},
              updatedAt: datePublished,
              version: 3,
            },
          ].map((deployment, idx) => (
            <IntegrationDetailHistoryListViewItem
              key={idx}
              actions={action('onActionClicked')}
              currentState={'Published'}
              i18nRunning={'Running'}
              i18nTextLastPublished={'Last published on '}
              i18nTextVersion={'Version'}
              updatedAt={'' + deployment.updatedAt}
              version={deployment.version}
            />
          ))}
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
          <IntegrationDetailInfo
            name={'Integration name'}
            version={1}
            currentState={'Published'}
            i18nLogUrlText={'log url'}
            i18nProgressPending={'progress pending'}
            i18nProgressStarting={'progress starting'}
            i18nProgressStopping={'progress stopping'}
            targetState={'Published'}
          />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
        </Container>
        <IntegrationDetailActivity
          i18nBtnRefresh={'Refresh'}
          i18nLastRefresh={'Last refresh'}
          i18nViewLogOpenShift={'View Log in OpenShift'}
          linkToOpenShiftLog={'/link'}
          children={activityItems}
          onRefresh={action('onRefresh')}
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
          <IntegrationDetailInfo
            name={'Integration name'}
            version={1}
            currentState={'Published'}
            i18nLogUrlText={'log url'}
            i18nProgressPending={'progress pending'}
            i18nProgressStarting={'progress starting'}
            i18nProgressStopping={'progress stopping'}
            targetState={'Published'}
          />
          <TabBar>
            <TabBarItem label={'Details'} to={'#details'} />
            <TabBarItem label={'Activity'} to={'#activity'} />
            <TabBarItem label={'Metrics'} to={'#metrics'} />
          </TabBar>
          <IntegrationDetailMetrics
            i18nLastProcessed={'Last Processed'}
            i18nNoDataAvailable={'No data available'}
            i18nSince={'Since '}
            i18nTotalErrors={'Total Errors'}
            i18nTotalMessages={'Total Messages'}
            i18nUptime={'Uptime'}
            errors={2}
            lastProcessed={'2 May 2019 08:19:42 GMT'}
            messages={26126}
            start={2323342333}
            uptimeDuration={'3:15:59'}
          />
        </Container>
      </>
    </Router>
  ));
