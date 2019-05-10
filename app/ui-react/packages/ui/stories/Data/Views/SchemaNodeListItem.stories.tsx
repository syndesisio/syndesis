import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { SchemaNodeListItem } from '../../../src';

const stories = storiesOf(
  'Data/Virtualizations/Views/SchemaNodeListItem',
  module
);

const nodeName = 'Customers';
const selectionChangedActionText = 'Selection changed for ' + nodeName;

stories.add('sample schema node item', () => (
  <SchemaNodeListItem
    key="nodeListItem2"
    name={text('name', nodeName)}
    connectionName={'connection1'}
    schemaPath={'schema=public/table=customers'}
    selected={false}
    onSelectionChanged={action(selectionChangedActionText)}
  />
));
