import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { DvMetricsContainer } from '../../../src';

const stories = storiesOf('Data/Metrics/MetricsTab', module);
stories.addDecorator(withKnobs);

const title = 'No metrics data available';
const description = 'There is no Metrics details available for this virtualization.';

const requestCount = 988;
const cacheHitRatioPct = '35%';
const sessions = 0;
const startedAt = 'Since 10 December 3pm';

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
'Empty Metrics State',
  () => (
    <DvMetricsContainer
      i18nNoDataTitle={title}
      i18nNoDataDescription={description}
      />
  ),
  { notes: 'Check If the Empty state message is shown.' }
)
.add(
    'Metrics Tab',
      () => (
        <DvMetricsContainer
        resultSetCacheProps={{
          a11yInfoCloseButton: 'Close cache hit ratio popover',
          a11yInfoPopover: 'Cache hit ratio popover',
          cacheHitRatioPercentage: cacheHitRatioPct,
          i18nCacheHitRatioText: 'Cache hit ratio',
          i18nInfoMessage: 'Cache hit ratios information message goes here.',
          i18nNoData: 'No data available',
          i18nTitle: 'Result set cache',
          loading: false,
        }}
        clientSessionProps={{
          i18nNoData: 'No data available',
          i18nSessionText: 'Client sessions',
          i18nTitle: 'Client sessions',
          loading: false,
          sessionCount: sessions,
        }}
        i18nNoDataTitle={'No metrics data available'}
        i18nNoDataDescription={'There is no Metrics details available for this virtualization.'}
        requestProps={{
          i18nNoData: 'No data available',
          i18nRequestText: 'Total Requests',
          i18nTitle: 'Requests',
          loading: false,
          requestCount,
        }}
        uptimeProps={{
          i18nNoData: 'No data available',
          i18nSinceMessage: startedAt,
          i18nTitle: 'DV Pod Uptime',
          i18nUptime: '1 day 3 hours 9 minutes',
          loading: false,
        }}
      />
      ),
      { notes: storyNotes }
    );
