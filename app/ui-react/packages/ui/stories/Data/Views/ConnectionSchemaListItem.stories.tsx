import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { ConnectionSchemaListItem, ConnectionStatus } from '../../../src';

const stories = storiesOf('Data/Views/ConnectionSchemaListItem', module);

const connectionName = 'Connection_1';
const connectionDescription = 'Connection_1 description';

stories.add('ACTIVE, not loading', () => (
  <ConnectionSchemaListItem
    connectionName={connectionName}
    connectionDescription={connectionDescription}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.ACTIVE}
    dvStatusError={<div>The connection is active</div>}
    i18nRefreshInProgress={text('i18nRefreshInProgress', 'Refresh in progress...')}
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
    dvStatusError={<div>The connection is active</div>}
    i18nRefreshInProgress={text('i18nRefreshInProgress', 'Refresh in progress...')}
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
    dvStatusError={<div>The connection is inactive</div>}
    i18nRefreshInProgress={text('i18nRefreshInProgress', 'Refresh in progress...')}
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
    dvStatusError={<div>Alert component goes here</div>}
    i18nRefreshInProgress={text('i18nRefreshInProgress', 'Refresh in progress...')}
    icon={<div />}
    loading={false}
  />
));
