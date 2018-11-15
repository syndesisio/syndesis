import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { StoryHelper } from '../.storybook/StoryHelper';
import { AggregatedMetricCard } from '../src';

const stories = storiesOf('Components', module);

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add('AggregatedMetricCard', () => (
    <AggregatedMetricCard title={'A Title'} ok={10} error={5} />
  ));
