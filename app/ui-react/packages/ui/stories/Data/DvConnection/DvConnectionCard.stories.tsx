import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
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
      dvStatusMessage={'The connection is active'}
      i18nRefreshInProgress={text(
        'i18nRefreshInProgress',
        'Refresh in progress...'
      )}
      i18nStatusErrorPopoverTitle={'Connection Error'}
      i18nStatusErrorPopoverLink={'Connection error'}
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
      dvStatusMessage={'The connection is active'}
      i18nRefreshInProgress={text(
        'i18nRefreshInProgress',
        'Refresh in progress...'
      )}
      i18nStatusErrorPopoverTitle={'Connection Error'}
      i18nStatusErrorPopoverLink={'Connection error'}
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
      dvStatusMessage={'The connection is inactive'}
      i18nRefreshInProgress={text(
        'i18nRefreshInProgress',
        'Refresh in progress...'
      )}
      i18nStatusErrorPopoverTitle={'Connection Error'}
      i18nStatusErrorPopoverLink={'Connection error'}
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
      dvStatusMessage={'The server exception is shown here'}
      i18nRefreshInProgress={text(
        'i18nRefreshInProgress',
        'Refresh in progress...'
      )}
      i18nStatusErrorPopoverTitle={'Connection Error'}
      i18nStatusErrorPopoverLink={'Connection error'}
      icon={<div />}
      loading={false}
      selected={false}
      onSelectionChanged={action('selection changed')}
    />
  );
});
