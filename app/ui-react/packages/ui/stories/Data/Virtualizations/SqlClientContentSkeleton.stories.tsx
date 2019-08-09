import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { SqlClientContentSkeleton } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/SqlClientContentSkeleton',
  module
);

stories.add('render', () => <SqlClientContentSkeleton />);
