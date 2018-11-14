import { storiesOf } from '@storybook/react';
import { specs } from 'storybook-addon-specifications';
import * as React from 'react';
import '../.storybook/test';
import { StoryHelper } from '../.storybook/StoryHelper';
import { AggregatedMetricCard } from '../src';

const stories = storiesOf('Components', module);

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add('AggregatedMetricCard', () => {
    specs(() => require('../tests/AggregatedMetricCard.spec').default);

    return <AggregatedMetricCard title={'A Title'} ok={10} error={5} />;
  });
