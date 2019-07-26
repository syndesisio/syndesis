import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { ViewInfoListItem } from '../../../src';

const stories = storiesOf(
  'Data/Views/ViewInfoListItem',
  module
);

const viewName = 'Customers';
const viewDesc = 'Description for Customers';
const selectionChangedActionText = 'Selection changed for view ' + viewName;

const sampleSourceTableNotes =
  '- Verify view icon is showing on the left\n' +
  '- Verify view name is "' +
  viewName +
  '"\n' +
  '- Verify view description is "' +
  viewDesc +
  '"';

stories.add(
  'sample source table item',
  withNotes(sampleSourceTableNotes)(() => (
    <ViewInfoListItem
      key="viewListItem2"
      name={text('name', viewName)}
      description={text('description', viewDesc)}
      connectionName={'connection1'}
      nodePath={[]}
      selected={false}
      i18nUpdate={'Update'}
      isUpdateView={false}
      onSelectionChanged={action(selectionChangedActionText)}
    />
  ))
);
