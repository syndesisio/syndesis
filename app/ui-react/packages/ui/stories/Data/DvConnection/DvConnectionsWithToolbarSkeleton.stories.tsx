import { storiesOf } from '@storybook/react';
import * as React from 'react';

import {
  DvConnectionsGridCell,
  DvConnectionSkeleton,
  DvConnectionsToolbarSkeleton,
} from '../../../src';

const stories = storiesOf(
  'Data/DvConnection/DvConnectionsWithToolbarSkeleton',
  module
);

stories.add('render', () => (
  <>
    <DvConnectionsToolbarSkeleton />
    {new Array(5).fill(0).map((_, index) => (
      <DvConnectionsGridCell key={index}>
        <DvConnectionSkeleton />
      </DvConnectionsGridCell>
    ))}
  </>
));
