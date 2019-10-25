import { storiesOf } from '@storybook/react';
import * as React from 'react';

import {
  DvConnectionsGrid,
  DvConnectionsGridCell,
  DvConnectionSkeleton,
  DvConnectionsToolbarSkeleton,
} from '../../../src';

const stories = storiesOf(
  'Data/DvConnection/DvConnectionsToolbarSkeleton',
  module
);

stories.add('render', () => (
  <React.Fragment>
    <DvConnectionsToolbarSkeleton />
    <DvConnectionsGrid>
      {new Array(5).fill(0).map((_, index) => (
        <DvConnectionsGridCell key={index}>
          <DvConnectionSkeleton />
        </DvConnectionsGridCell>
      ))}
    </DvConnectionsGrid>
  </React.Fragment>
));
