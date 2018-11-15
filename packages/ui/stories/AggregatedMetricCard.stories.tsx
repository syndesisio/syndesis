import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { text, number } from '@storybook/addon-knobs';
import { StoryHelper } from '../.storybook/StoryHelper';
import { AggregatedMetricCard } from '../src';

const stories = storiesOf('Components', module);

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add('AggregatedMetricCard', () => (
    <AggregatedMetricCard
      title={text('title', 'A Title')}
      ok={number('ok', 10)}
      error={number('error', 5)}
    />
  ));
