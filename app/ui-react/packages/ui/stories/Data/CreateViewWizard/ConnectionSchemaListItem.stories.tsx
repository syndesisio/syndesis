import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { ConnectionSchemaListItem, ConnectionStatus } from '../../../src';

const stories = storiesOf('Data/CreateViewWizard/ConnectionSchemaListItem', module);

const connectionName = 'Connection_1';
const connectionDescription = 'Connection_1 description';

stories.add('ACTIVE, not loading', () => (
  <ConnectionSchemaListItem
    connectionName={connectionName}
    connectionDescription={connectionDescription}
    haveSelectedSource={false}
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
  />
));

stories.add('ACTIVE, loading', () => (
  <ConnectionSchemaListItem
    connectionName={connectionName}
    connectionDescription={connectionDescription}
    haveSelectedSource={false}
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
  />
));

stories.add('INACTIVE, loading', () => (
  <ConnectionSchemaListItem
    connectionName={connectionName}
    connectionDescription={connectionDescription}
    haveSelectedSource={false}
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
  />
));

stories.add('FAILED, not loading', () => (
  <ConnectionSchemaListItem
    connectionName={connectionName}
    connectionDescription={connectionDescription}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.FAILED}
    dvStatusMessage={'The server exception is displayed here'}
    i18nRefreshInProgress={text(
      'i18nRefreshInProgress',
      'Refresh in progress...'
    )}
    i18nStatusErrorPopoverTitle={'Connection Error'}
    i18nStatusErrorPopoverLink={'Connection error'}
    icon={<div />}
    loading={false}
  />
));
