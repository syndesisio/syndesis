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
      dvStatusTooltip={'The connection is active'}
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
      dvStatusTooltip={'The connection is active'}
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
      dvStatusTooltip={'The connection is inactive'}
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
      dvStatusTooltip={'The server exception is shown here'}
      icon={<div />}
      loading={false}
      selected={false}
      onSelectionChanged={action('selection changed')}
    />
  );
});
