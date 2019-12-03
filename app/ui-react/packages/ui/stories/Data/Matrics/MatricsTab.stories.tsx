import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { DvMetricsContainer } from '../../../src';

const stories = storiesOf('Data/Matrics/MatricsTab', module);
stories.addDecorator(withKnobs);

const title = 'No metrics data available';
const description = 'There is no Metrics details available for this virtualization.';

const storyNotes =
  '- Verify the cards Are shown.' +
  '\n' +
  '- Verify Client Sessions card is shown.' +
  '\n' +
  '- Verify the Total Request card is shown.' +
  '\n' +
  '- Verify Cache hit ratios card is shown.' +
  '\n'+
  '- Verify Uptime Card is shown.';


stories
.add(
'Empty Matrics State',
  () => (
    <DvMetricsContainer
      i18nNoDataTitle={title}
      i18nNoDataDescription={description}
      />
  ),
  { notes: 'Check If the Empty state message is shown.' }
)
.add(
    'Matrics Tab',
      () => (
        <DvMetricsContainer
        cacheHitProps={{
          a11yInfoCloseButton: 'Close info popover',
          a11yInfoPopover: 'Info popover',
          i18nDatetime: 'Nov 18, 11:40:00 pm',
          i18nInfoMessage: 'Cache hit ratios information message goes here.',
          i18nNoData: 'No data available',
          i18nTitle: 'Cache hit ratios',
          loading: false,
          percentage: '35%',
        }}
        clientSessionProps={{
          connectionCount: 8,
          i18nConnectionMessage: 'Connections are issuing queries',
          i18nNoData: 'No data available',
          i18nTitle: 'Client sessions',
          i18nViewAllAction: 'View all',
          loading: false,
          onViewAll: () => alert('Implement View all'),
        }}
        i18nNoDataTitle={'No metrics data available'}
        i18nNoDataDescription={'There is no Metrics details available for this virtualization.'}
        requestProps={{
          a11yShowFailed: 'Show Failed Requests',
          a11yShowSucceeded: 'Show Succeeded Requests',
          failedCount: 129,
          i18nNoData: 'No data available',
          i18nTitle: 'Total requests',
          loading: false,
          onShowFailed: () => alert('Implement Show Failed'),
          onShowSucceeded: () => alert('Implement Show Succeeded'),
          successCount: 17000,
        }}
        uptimeProps={{
          i18nNoData: 'No data available',
          i18nSinceMessage: 'Since Oct 11, 11:47:14 pm',
          i18nTitle: 'Uptime',
          i18nUptime: '1 day 3 hours 9 minutes',
          loading: false,
        }}
      />
      ),
      { notes: storyNotes }
    );
