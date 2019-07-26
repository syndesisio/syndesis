import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { ConnectionSchemaListItem } from '../../../src';

const stories = storiesOf(
  'Data/Views/ConnectionSchemaListItem',
  module
);

const connectionName = 'Connection_1';
const connectionDescription = 'Connection_1 description';

stories.add('sample connection schema item', () => (
  <ConnectionSchemaListItem
    connectionName={connectionName}
    connectionDescription={connectionDescription}
  />
));
