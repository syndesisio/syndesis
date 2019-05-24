import { number, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AggregatedMetricCard } from '../../src';

const stories = storiesOf('Shared/AggregatedMetricCard', module);

stories.add('sample usage', () => (
  <div style={{ width: 200 }}>
    <AggregatedMetricCard
      title={text('title', 'A Title')}
      total={number('total', 15)}
      ok={number('ok', 10)}
      error={number('error', 5)}
    />
  </div>
));
