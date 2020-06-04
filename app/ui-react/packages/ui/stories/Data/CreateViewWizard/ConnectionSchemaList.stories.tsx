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

const stories = storiesOf('Data/CreateViewWizard/ConnectionSchemaList', module);

const connectionName1 = 'Connection_1';
const connectionDescription1 = 'Connection 1 description';
const connectionName2 = 'Connection_2';
const connectionDescription2 = 'Connection 2 description';
const connectionName3 = 'Connection_3';
const connectionDescription3 = 'Connection 3 description';
const connectionName4 = 'VirtSchema';
const connectionDescription4 = 'VirtSchema description';
const selectionChangedActionText = 'Selection Changed';

const conn2NodeItems = [
  <SchemaNodeListItem
    key="conn2Item1"
    name={'Customer'}
    teiidName={'Customer'}
    connectionName={connectionName2}
    isVirtualizationSchema={false}
    nodePath={['public', 'Customer']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
  <SchemaNodeListItem
    key="conn2Item2"
    name={'Account'}
    teiidName={'Account'}
    connectionName={connectionName2}
    isVirtualizationSchema={false}
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
    isVirtualizationSchema={false}
    nodePath={['public', 'Holdings']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
  <SchemaNodeListItem
    key="conn3Item2"
    name={'Product'}
    teiidName={'Product'}
    connectionName={connectionName3}
    isVirtualizationSchema={false}
    nodePath={['public', 'Product']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
];
const conn4NodeItems = [
  <SchemaNodeListItem
    key="conn4Item1"
    name={'View1'}
    teiidName={'View1'}
    connectionName={connectionName4}
    isVirtualizationSchema={true}
    nodePath={['View1']}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />,
  <SchemaNodeListItem
    key="conn4Item2"
    name={'View2'}
    teiidName={'View2'}
    connectionName={connectionName4}
    nodePath={['View2']}
    isVirtualizationSchema={true}
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
    dvStatusMessage={'The connection is active'}
    i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx.yy.zz')}
    i18nRefresh={text('i18nRefresh', 'Refresh')}
    i18nRefreshInProgress={text(
      'i18nRefreshInProgress',
      'Refresh in progress...'
    )}
    i18nStatusErrorPopoverTitle={'Connection Problem'}
    i18nStatusErrorPopoverLink={'Show connection problem'}
    icon={<div />}
    isVirtualizationSource={false}
    loading={false}
    refreshConnectionSchema={action('refreshConnectionSchema')}
  />,
  <ConnectionSchemaListItem
    key="connectionListItem2"
    connectionName={connectionName2}
    connectionDescription={connectionDescription2}
    children={conn2NodeItems}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.ACTIVE}
    dvStatusMessage={'The connection is active'}
    i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx.yy.zz')}
    i18nRefresh={text('i18nRefresh', 'Refresh')}
    i18nRefreshInProgress={text(
      'i18nRefreshInProgress',
      'Refresh in progress...'
    )}
    i18nStatusErrorPopoverTitle={'Connection Problem'}
    i18nStatusErrorPopoverLink={'Show connection problem'}
    icon={<div />}
    isVirtualizationSource={false}
    loading={false}
    refreshConnectionSchema={action('refreshConnectionSchema')}
  />,
  <ConnectionSchemaListItem
    key="connectionListItem3"
    connectionName={connectionName3}
    connectionDescription={connectionDescription3}
    children={conn3NodeItems}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.ACTIVE}
    dvStatusMessage={'The connection is active'}
    i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx.yy.zz')}
    i18nRefresh={text('i18nRefresh', 'Refresh')}
    i18nRefreshInProgress={text(
      'i18nRefreshInProgress',
      'Refresh in progress...'
    )}
    i18nStatusErrorPopoverTitle={'Connection Problem'}
    i18nStatusErrorPopoverLink={'Show connection problem'}
    icon={<div />}
    isVirtualizationSource={false}
    loading={false}
    refreshConnectionSchema={action('refreshConnectionSchema')}
  />,
  <ConnectionSchemaListItem
    key="virtualizationSchema"
    connectionName={connectionName4}
    connectionDescription={connectionDescription4}
    children={conn4NodeItems}
    haveSelectedSource={false}
    dvStatus={ConnectionStatus.ACTIVE}
    dvStatusMessage={'The connection is active'}
    i18nLastUpdatedMessage={text('i18nLastUpdatedMessage', 'Last updated: xx.yy.zz')}
    i18nRefresh={text('i18nRefresh', 'Refresh')}
    i18nRefreshInProgress={text(
      'i18nRefreshInProgress',
      'Refresh in progress...'
    )}
    i18nStatusErrorPopoverTitle={'Connection Problem'}
    i18nStatusErrorPopoverLink={'Show connection problem'}
    icon={<div />}
    isVirtualizationSource={true}
    loading={false}
    refreshConnectionSchema={action('refreshConnectionSchema')}
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
        linkToConnectionCreate={'/connections/create'}
      />
    </Router>
  ))

  .add('4 connections', () => (
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
        linkToConnectionCreate={'/connections/create'}
      />
    </Router>
  ));
