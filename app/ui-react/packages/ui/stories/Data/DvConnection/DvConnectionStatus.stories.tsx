import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ConnectionStatus,
  DvConnectionStatus,
} from '../../../src';

const stories = storiesOf(
  'Data/DvConnection/DvConnectionStatus',
  module
);

stories
  .add('ACTIVE, not loading', () => {
    return (
      <DvConnectionStatus
        dvStatus={ConnectionStatus.ACTIVE}
        dvStatusMessage={'The connection is active'}
        i18nRefreshInProgress={text(
          'i18nRefreshInProgress',
          'Refresh in progress...'
        )}
        i18nStatusErrorPopoverTitle={'Connection Error'}
        i18nStatusErrorPopoverLink={'Connection error'}
        loading={false}
      />
    );
  })

  .add('ACTIVE, loading', () => {
    return (
      <DvConnectionStatus
        dvStatus={ConnectionStatus.ACTIVE}
        dvStatusMessage={'The connection is active'}
        i18nRefreshInProgress={text(
          'i18nRefreshInProgress',
          'Refresh in progress...'
        )}
        i18nStatusErrorPopoverTitle={'Connection Error'}
        i18nStatusErrorPopoverLink={'Connection error'}
        loading={true}
      />
    );
  })

  .add('INACTIVE, not loading', () => {
    return (
      <DvConnectionStatus
        dvStatus={ConnectionStatus.INACTIVE}
        dvStatusMessage={'The connection is inactive'}
        i18nRefreshInProgress={text(
          'i18nRefreshInProgress',
          'Refresh in progress...'
        )}
        i18nStatusErrorPopoverTitle={'Connection Error'}
        i18nStatusErrorPopoverLink={'Connection error'}
        loading={false}
      />
    );
  })

  .add('FAILED, not loading', () => {
    return (
      <DvConnectionStatus
        dvStatus={ConnectionStatus.FAILED}
        dvStatusMessage={'The server exception is shown here'}
        i18nRefreshInProgress={text(
          'i18nRefreshInProgress',
          'Refresh in progress...'
        )}
        i18nStatusErrorPopoverTitle={'Connection Error'}
        i18nStatusErrorPopoverLink={'Connection error'}
        loading={false}
      />
    );
  })

  .add('FAILED, loading', () => {
    return (
      <DvConnectionStatus
        dvStatus={ConnectionStatus.FAILED}
        dvStatusMessage={'The server exception is shown here'}
        i18nRefreshInProgress={text(
          'i18nRefreshInProgress',
          'Refresh in progress...'
        )}
        i18nStatusErrorPopoverTitle={'Connection Error'}
        i18nStatusErrorPopoverLink={'Connection error'}
        loading={true}
      />
    );
  });
