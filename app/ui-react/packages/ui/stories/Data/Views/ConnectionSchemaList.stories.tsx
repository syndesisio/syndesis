import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import {
  ConnectionSchemaList,
  ConnectionSchemaListItem,
  ConnectionStatus,
  SchemaNodeListItem,
} from '../../../src';

const stories = storiesOf('Data/Views/ConnectionSchemaList', module);

const connectionName1 = 'Connection_1';
const connectionDescription1 = 'Connection 1 description';
const connectionName2 = 'Connection_2';
const connectionDescription2 = 'Connection 2 description';
const connectionName3 = 'Connection_3';
const connectionDescription3 = 'Connection 3 description';
const selectionChangedActionText = 'Selection Changed';

const conn2NodeItems = [
  <SchemaNodeListItem
    key="conn2Item1"
    name={'Customer'}
    teiidName={'Customer'}
    connectionName={connectionName2}
    nodePath={['public', 'Customer']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
  <SchemaNodeListItem
    key="conn2Item2"
    name={'Account'}
    teiidName={'Account'}
    connectionName={connectionName2}
    nodePath={['public', 'Account']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
];
const conn3NodeItems = [
  <SchemaNodeListItem
    key="conn3Item1"
    name={'Holdings'}
    teiidName={'Holdings'}
    connectionName={connectionName3}
    nodePath={['public', 'Holdings']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
  <SchemaNodeListItem
    key="conn3Item2"
    name={'Product'}
    teiidName={'Product'}
    connectionName={connectionName3}
    nodePath={['public', 'Product']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
];

const connectionItems = [
  <ConnectionSchemaListItem
    key="connectionListItem1"
    connectionName={connectionName1}
    connectionDescription={connectionDescription1}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.ACTIVE}
    dvStatusTooltip={'The connection is active'}
    i18nRefreshInProgress={text('i18nRefreshInProgress', 'Refresh in progress...')}
    icon={<div />}
    loading={false}
  />,
  <ConnectionSchemaListItem
    key="connectionListItem2"
    connectionName={connectionName2}
    connectionDescription={connectionDescription2}
    children={conn2NodeItems}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.ACTIVE}
    dvStatusTooltip={'The connection is active'}
    i18nRefreshInProgress={text('i18nRefreshInProgress', 'Refresh in progress...')}
    icon={<div />}
    loading={false}
  />,
  <ConnectionSchemaListItem
    key="connectionListItem3"
    connectionName={connectionName3}
    connectionDescription={connectionDescription3}
    children={conn3NodeItems}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.ACTIVE}
    dvStatusTooltip={'The connection is active'}
    i18nRefreshInProgress={text('i18nRefreshInProgress', 'Refresh in progress...')}
    icon={<div />}
    loading={false}
  />,
];

const emptyStateTitle = 'No Active Connections';
const emptyStateMsg =
  'There are no active connections available. Click Create Connection for new connection.';

stories

  .add('empty list', () => (
    <Router>
      <ConnectionSchemaList
        i18nEmptyStateInfo={text('i18nEmptyStateInfo', emptyStateMsg)}
        i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
        i18nLinkCreateConnection={text(
          'i18nLinkCreateConnection',
          'Create Connection'
        )}
        children={[]}
        hasListData={false}
        loading={false}
        linkToConnectionCreate={action('route to create connection')}
      />
    </Router>
  ))

  .add('3 connections', () => (
    <Router>
      <ConnectionSchemaList
        i18nEmptyStateInfo={text('i18nEmptyStateInfo', emptyStateMsg)}
        i18nEmptyStateTitle={text('i18nEmptyStateTitle', emptyStateTitle)}
        i18nLinkCreateConnection={text(
          'i18nLinkCreateConnection',
          'Create Connection'
        )}
        children={connectionItems}
        hasListData={true}
        loading={false}
        linkToConnectionCreate={action('route to create connection')}
      />
    </Router>
  ));
