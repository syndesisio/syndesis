import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import {
  ConnectionStatus,
  DvConnectionCard,
} from '../../../src';

const stories = storiesOf(
  'Data/DvConnection/DvConnectionCard',
  module
);

stories.add('ACTIVE, not loading, not selected', () => {
  return (
    <DvConnectionCard
      name={'Connection1'}
      description={'Connection1 description'}
      dvStatus={ConnectionStatus.ACTIVE}
      icon={<div />}
      loading={false}
      selected={false}
      onSelectionChanged={action('selection changed')}
    />
  );
})

stories.add('ACTIVE, loading, not selected', () => {
  return (
    <DvConnectionCard
      name={'Connection1'}
      description={'Connection1 description'}
      dvStatus={ConnectionStatus.ACTIVE}
      icon={<div />}
      loading={true}
      selected={false}
      onSelectionChanged={action('selection changed')}
    />
  );
})

.add('INACTIVE, loading, not selected', () => {
  return (
    <DvConnectionCard
      name={'Connection1'}
      description={'Connection1 description'}
      dvStatus={ConnectionStatus.INACTIVE}
      icon={<div />}
      loading={true}
      selected={false}
      onSelectionChanged={action('selection changed')}
    />
  );
})

.add('FAILED, not loading, not selected', () => {
  return (
    <DvConnectionCard
      name={'Connection1'}
      description={'Connection1 description'}
      dvStatus={ConnectionStatus.FAILED}
      icon={<div />}
      loading={false}
      selected={false}
      onSelectionChanged={action('selection changed')}
    />
  );
});
