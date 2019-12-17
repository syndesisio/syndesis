import { storiesOf } from '@storybook/react';
import * as React from 'react';

import {
  DvMetricsContainerSkeleton,
} from '../../../src';

const stories = storiesOf(
  'Data/Metrics/DvMetricsContainerSkeleton',
  module
);

stories.add('render', () => (
  <React.Fragment>
    <DvMetricsContainerSkeleton />
  </React.Fragment>
));
